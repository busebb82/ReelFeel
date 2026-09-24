const form = document.getElementById("mood-form");
const moodInput = document.getElementById("mood");
const genresBox = document.getElementById("genres");
const recommendButton = document.getElementById("recommend-button");
const moreButton = document.getElementById("more-button");
const errorBox = document.getElementById("error");
const results = document.getElementById("results");
const recentRow = document.getElementById("recent");

const HISTORY_KEY = "reelfeel-son-aramalar";
const MAX_HISTORY = 5;

let selectedGenre = null;
let shownTitles = [];

async function loadGenres() {
    const response = await fetch("/api/genres");
    const genres = await response.json();

    genresBox.appendChild(createGenreChip("Hepsi", null));
    for (const genre of genres) {
        genresBox.appendChild(createGenreChip(genre, genre));
    }
    genresBox.firstChild.classList.add("selected");
}

function createGenreChip(label, value) {
    const chip = document.createElement("button");
    chip.type = "button";
    chip.className = "chip";
    chip.textContent = label;
    chip.addEventListener("click", () => {
        genresBox.querySelectorAll(".chip").forEach(c => c.classList.remove("selected"));
        chip.classList.add("selected");
        selectedGenre = value;
    });
    return chip;
}

async function recommend(isMore) {
    const mood = moodInput.value.trim();
    if (mood === "") {
        showError("Önce ruh halini yazmalısın.");
        moodInput.focus();
        return;
    }
    if (!isMore) {
        shownTitles = [];
        saveToHistory(mood);
    }

    setLoading(true);
    try {
        const response = await fetch("/api/recommendations", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ mood: mood, genre: selectedGenre, excluded: shownTitles })
        });
        const data = await response.json();
        if (!response.ok) {
            throw new Error(data.message || "Bir hata oluştu, tekrar dene.");
        }

        showFilms(data);
        shownTitles.push(...data.map(film => film.title));
        moreButton.hidden = false;
    } catch (error) {
        results.innerHTML = "";
        showError(error instanceof TypeError ? "Sunucuya bağlanılamadı." : error.message);
    } finally {
        setLoading(false);
    }
}

function setLoading(loading) {
    recommendButton.disabled = loading;
    moreButton.disabled = loading;
    recommendButton.textContent = loading ? "Aranıyor..." : "Film Öner";

    if (loading) {
        errorBox.hidden = true;
        results.innerHTML = "";
        for (let i = 0; i < 3; i++) {
            const skeleton = document.createElement("div");
            skeleton.className = "skeleton";
            results.appendChild(skeleton);
        }
    }
}

function showError(message) {
    errorBox.textContent = message;
    errorBox.hidden = false;
}

function showFilms(films) {
    results.innerHTML = "";

    const title = document.createElement("h2");
    title.className = "results-title";
    title.textContent = "Senin için seçtiklerimiz";
    results.appendChild(title);

    films.forEach((film, index) => {
        const card = createFilmCard(film);
        card.style.animationDelay = (index * 0.08) + "s";
        results.appendChild(card);
    });
    title.scrollIntoView({ behavior: "smooth", block: "start" });
}

function createFilmCard(film) {
    const card = document.createElement("article");
    card.className = "film-card";

    let poster;
    if (film.posterUrl) {
        poster = document.createElement("img");
        poster.src = film.posterUrl;
        poster.alt = film.title + " afişi";
        poster.loading = "lazy";
    } else {
        poster = document.createElement("div");
        poster.textContent = "🎬";
    }
    poster.classList.add("poster");
    card.appendChild(poster);

    const info = document.createElement("div");
    info.className = "film-info";
    info.appendChild(createElement("h3", "film-title", film.title));

    const meta = createElement("div", "film-meta", "");
    meta.appendChild(createElement("span", "", String(film.year)));
    if (film.rating > 0) {
        meta.appendChild(createElement("span", "rating", "★ " + film.rating.toFixed(1)));
    }
    info.appendChild(meta);

    const reason = createElement("p", "reason", film.reason);
    reason.prepend(createElement("strong", "", "Neden sana uygun?"));
    info.appendChild(reason);

    if (film.overview) {
        const overview = createElement("p", "overview", film.overview);
        overview.title = "Tamamını görmek için tıkla";
        overview.addEventListener("click", () => overview.classList.toggle("expanded"));
        info.appendChild(overview);
    }

    card.appendChild(info);
    return card;
}

function createElement(tag, className, text) {
    const element = document.createElement(tag);
    if (className) {
        element.className = className;
    }
    element.textContent = text;
    return element;
}

function getHistory() {
    try {
        return JSON.parse(localStorage.getItem(HISTORY_KEY)) || [];
    } catch (e) {
        return [];
    }
}

function saveToHistory(mood) {
    const history = getHistory().filter(item => item !== mood);
    history.unshift(mood);
    try {
        localStorage.setItem(HISTORY_KEY, JSON.stringify(history.slice(0, MAX_HISTORY)));
    } catch (e) {
    }
    showHistory();
}

function showHistory() {
    recentRow.querySelectorAll(".chip").forEach(chip => chip.remove());
    const history = getHistory();
    recentRow.hidden = history.length === 0;

    for (const mood of history) {
        const chip = document.createElement("button");
        chip.type = "button";
        chip.className = "chip";
        chip.textContent = mood.length > 40 ? mood.slice(0, 40) + "…" : mood;
        chip.title = mood;
        chip.addEventListener("click", () => {
            moodInput.value = mood;
            moodInput.focus();
        });
        recentRow.appendChild(chip);
    }
}

form.addEventListener("submit", event => {
    event.preventDefault();
    recommend(false);
});

moreButton.addEventListener("click", () => recommend(true));

moodInput.addEventListener("keydown", event => {
    if (event.key === "Enter" && (event.ctrlKey || event.metaKey)) {
        recommend(false);
    }
});

document.querySelectorAll(".example").forEach(chip => {
    chip.addEventListener("click", () => {
        moodInput.value = chip.textContent;
        moodInput.focus();
    });
});

loadGenres();
showHistory();
