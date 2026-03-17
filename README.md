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

Questo progetto è rivolto a **sviluppatori esperti**:

- Viene fornito **solo il codice sorgente** (nessun `.jar`)
- Le **dipendenze non sono incluse**
- Documentazione limitata ai commenti nel codice
- Alcune funzionalità potrebbero essere legacy o specifiche del progetto originale
- **Manutenzione a carico della community**

📌 Requisiti:
- Java compatibile con Spigot/Paper
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

## ❤️ Contributi

Essendo una fork community-driven:

- Non garantisco aggiornamenti per mancanza di tempo
- Pull request benvenute
- Segnalazioni bug apprezzate
- Migliorie cross-version molto utili
