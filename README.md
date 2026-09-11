# PROGETTO TUTORE

App Android M&P Project per la progettazione parametrica e l'archiviazione di tutori artigianali per mano, polso e avambraccio.

## Funzioni presenti nella v0.2
- Interfaccia grafica rinnovata in Material 3
- Archivio pazienti persistente sul dispositivo
- Archivio dei tutori associati a ogni paziente
- Pulsanti di eliminazione paziente e singolo tutore con conferma
- Anteprima grafica dinamica mentre si inseriscono le misure
- Pezzi del tutore codificati con colori differenti
- Vista assemblata e vista esplosa del tutore
- Calcolo delle quote dei principali componenti
- Esportazione completa dei dati in JSON
- Importazione del backup JSON
- VersionCode automatico e crescente nelle build GitHub Actions
- Identificativo Android stabile: `it.progettotutore.app`

## Aggiornamenti senza perdita dati
I dati vengono conservati nello storage privato dell'app. Installando una versione più recente **sopra** quella esistente, Android mantiene i dati se restano invariati:
1. `applicationId`
2. firma digitale dell'APK
3. `versionCode` crescente

Il progetto mantiene automaticamente il primo requisito e genera automaticamente un `versionCode` crescente nelle build GitHub Actions.

### Firma digitale stabile
Perché gli APK futuri possano realmente sovrapporsi a quello già installato, GitHub Actions è predisposto per firmare ogni build con la stessa chiave. Nel repository vanno configurati una sola volta questi GitHub Actions Secrets:
- `ANDROID_KEYSTORE_BASE64`
- `ANDROID_KEY_ALIAS`
- `ANDROID_KEYSTORE_PASSWORD`
- `ANDROID_KEY_PASSWORD`

Quando i secret sono presenti, il workflow produce `ProgettoTutore-update.apk` firmato con la chiave stabile. Senza questi secret viene creato un APK debug di prova, che non deve essere considerato il canale definitivo di aggiornamento.

Configurazione della firma permanente completata nel repository; le build successive vengono verificate tramite GitHub Actions.

Nuova build richiesta per verificare aggiornamento e firma permanente.

## Backup
Dalla schermata **Dati** si può esportare l'intero archivio in un file JSON e successivamente importarlo. È consigliato fare un backup prima di cambi importanti o trasferimenti su un altro dispositivo.

## Nota professionale
Le quote generate sono un supporto tecnico alla prototipazione. Indicazione clinica, fitting, pressioni, comfort, materiali e sicurezza del dispositivo devono essere verificati dal professionista sul paziente.

© 2026 M&P Project — Tutti i diritti riservati / All rights reserved.
