# 🌿 Wild Adventure Plugins – Community Fork

Questa repository è una **fork non ufficiale** dei plugin di Wild Adventure, mantenuta dalla community.

👉 Obiettivo del progetto:  
rendere il codice **compatibile sia con versioni legacy che moderne di Minecraft**.

---

## 🚧 Stato del progetto

- ✅ Supporto **Minecraft 1.8.8**
- 🚧 Supporto **Minecraft 1.21.11 (Work in Progress)**
- 🔧 Refactoring e miglioramenti continui

Questo fork introduce modifiche per migliorare:
- compatibilità cross-version
- stabilità del codice
- riutilizzabilità nei propri progetti

---

## ⚠️ Requisiti e Note

Questo progetto non è ancora **PRODUCTION READY**:

📌 Requisiti:
- Sorgente compilato con Java 21 (Raccomando di utilizzare SportPaper per 1.8.8)
- Lombok → https://projectlombok.org/

---

## 📦 WildCommons (Core)

Questa fork utilizza una libreria core condivisa:

```xml
<dependency>
  <groupId>it.danielebruni.wildadventure</groupId>
  <artifactId>wildcommons-core</artifactId>
  <version>1.0.1</version>
</dependency>
```

---

## 🚀 Build automatiche

Puoi scaricare le ultime build da GitHub Actions:

👉 https://github.com/danib150/WildCommons/actions

Utile per:
- testare versioni aggiornate
- sviluppo continuo
- accedere a build WIP

---

## 🧩 Differenze rispetto all’originale

Questa fork:

- 🔄 Aggiorna il codice per versioni moderne di Minecraft
- 🧹 Rimuove o adatta codice specifico di Wild Adventure
- ⚙️ Migliora la compatibilità con diversi ambienti server
- 🧪 Introduce modifiche sperimentali (soprattutto lato 1.21.x)

---

## 📜 Licenza

Distribuito sotto la licenza originale di Wild Adventure:

- ❌ Non puoi vendere questo plugin o derivati
- ✅ Puoi modificarlo e usarlo liberamente
- ✅ Puoi distribuirlo gratuitamente
- 📌 Devi includere la licenza originale
- 📌 Devi mantenere gli header nel codice
- ❌ Non puoi usare il nome *Wild Adventure* per promuovere derivati

⚠️ Gli autori non si assumono responsabilità per eventuali problemi.

---

## 🌍 Progetti che utilizzano WildCommons

Ecco alcuni progetti che utilizzano questa libreria core:

### 🎮 Rework di Plugin Minigame

* **Bedwars**
  🔗 https://github.com/danib150/Bedwars

* **SurvivalGames**
  🔗 https://github.com/danib150/SurvivalGames

* **PvPGames**
  🔗 https://github.com/danib150/PvPGames

* **SkyWars**
  🔗 https://github.com/danib150/SkyWars

* **HungerGames**
  🔗 https://github.com/danib150/HungerGames

### ⚙️ Utility & Core Addons

* **Boosters**
  🔗 https://github.com/danib150/Boosters (Richiesto come dipendenza dai plugin Minigames)
  
---
