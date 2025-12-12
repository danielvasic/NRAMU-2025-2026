# Tutorial: Implementacija CRUD operacija (Uređivanje, Brisanje i Notifikacije)

## Uvod

U prethodnim vježbama uspješno smo implementirali **dohvaćanje (Read)** i **kreiranje (Create)** podataka. Kako bismo upotpunili funkcionalnost naše aplikacije, današnji zadatak je omogućiti korisniku **izmjenu (Update)** i **brisanje (Delete)** vježbi, te slanje obavijesti korisniku kada je vježba obrisana.

Backend logika je već pripremljena u vašem repozitoriju, stoga će fokus biti na povezivanju korisničkog sučelja s postojećim metodama.

---

## 1. Analiza postojećeg koda (Backend)

Prije pisanja novog koda, ključno je razumjeti alate koje imamo na raspolaganju. 

### 1.1 Provjerite ExerciseRepository.java

Otvorite datoteku: `app/src/main/java/ba/sum/fsre/fitness/repository/ExerciseRepository.java`

Primijetit ćete da već postoje definirane metode:

```java
// Metoda za ažuriranje (Update) - prima ID i nove podatke
public Call<List<Excercise>> update(String id, String name, String desc, String imageUrl) {
    return api.updateExcercise(bearer(), "eq." + id, new ExcerciseRequest(name, desc, imageUrl));
}

// Metoda za brisanje (Delete) - prima samo ID
public Call<Void> delete(String id) {
    return api.deleteExcercise(bearer(), "eq." + id);
}
```

**Objašnjenje:**
- Metoda `update()` šalje HTTP PATCH zahtjev na Supabase sa novim podacima
- Metoda `delete()` šalje HTTP DELETE zahtjev koji trajno uklanja vježbu iz baze
- Obje metode koriste `bearer()` za autentifikaciju (automatski dodaje Bearer token)

---

## 2. Izrada korisničkog sučelja (XML)

Da bismo mogli uređivati podatke, potreban nam je novi ekran.

### 2.1 Kreirajte novi Layout

**Lokacija:** `app/src/main/res/layout/activity_edit_exercise.xml`

**Kako kreirati:**
1. U Android Studiju, desni klik na `res/layout`
2. Odaberite `New → Layout Resource File`
3. Naziv: `activity_edit_exercise`
4. Root element: `LinearLayout`

**Kod:**

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:padding="24dp"
    android:gravity="center_horizontal">

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Uredi vježbu"
        android:textSize="24sp"
        android:textStyle="bold"
        android:layout_marginBottom="32dp"/>

    <EditText
        android:id="@+id/editName"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:hint="Naziv vježbe"
        android:minHeight="48dp"/>

    <EditText
        android:id="@+id/editDesc"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:hint="Opis vježbe"
        android:minHeight="48dp"
        android:layout_marginBottom="24dp"/>

    <Button
        android:id="@+id/btnSave"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Spremi promjene"/>

    <Button
        android:id="@+id/btnDelete"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Obriši vježbu"
        android:backgroundTint="#D32F2F"
        android:textColor="#FFFFFF"/>
</LinearLayout>
```

**Objašnjenje elemenata:**

| Element | ID | Svrha |
|---------|-----|-------|
| TextView | - | Naslov ekrana ("Uredi vježbu") |
| EditText | editName | Polje za unos novog naziva vježbe |
| EditText | editDesc | Polje za unos novog opisa |
| Button | btnSave | Gumb za spremanje izmjena (zelena akcija) |
| Button | btnDelete | Gumb za brisanje (crvena boja upozorava na destruktivnu akciju) |

---

## 3. Priprema DashboardActivity

Moramo nadograditi `DashboardActivity` da "pamti" podatke i da se osvježi kada se vratimo s uređivanja.

### 3.1 Globalna lista i metoda za dohvaćanje

**Lokacija:** `app/src/main/java/ba/sum/fsre/fitness/activities/DashboardActivity.java`

#### Korak 1: Dodajte globalnu listu na početak klase

```java
public class DashboardActivity extends AppCompatActivity {
    // ...ostale varijable...
    
    // DODAJTE OVO:
    private List<Excercise> globalExerciseList = new ArrayList<>(); // Globalna lista
```

**Zašto?** Trebamo pristup podacima i izvan `onResponse` metode kako bismo ih mogli proslijediti kada korisnik klikne na stavku.

---

#### Korak 2: Izdvojite kod za dohvaćanje u metodu `fetchExercises()`

**Prije:**
```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_dashboard);
    
    // ... inicijalizacija ...
    
    // Ovdje je bio kod za RetrofitClient.getInstance().getApi().getAll()...
}
```

**Poslije:**
```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_dashboard);

    authManager = new AuthManager(this);

    // initialize UI first so buttons are ready even if network is slow
    initViews();
    setupListeners();

    // Prvo punjenje liste vježbi
    fetchExercises(); // POZIV NOVE METODE
}

// NOVA METODA koju možemo zvati više puta
private void fetchExercises() {
    this.workoutProgress.setVisibility(View.VISIBLE);

    String rawToken = authManager.getToken();
    String token = rawToken != null ? "Bearer " + rawToken : null;

    if (token == null) {
        Toast.makeText(DashboardActivity.this, "Niste prijavljeni. Prijavite se ponovo.", Toast.LENGTH_LONG).show();
        this.workoutProgress.setVisibility(View.INVISIBLE);
        return;
    }

    RetrofitClient
        .getInstance()
        .getApi()
        .getAll(token).enqueue(new Callback<List<Excercise>>() {
            @Override
            public void onResponse(Call<List<Excercise>> call, Response<List<Excercise>> response) {
                // ... ostali kod obrade odgovora ...
                
                if (response.isSuccessful() && response.body() != null) {
                    List<Excercise> excerciseList = response.body();
                    
                    // VAŽNO: Spremamo u globalnu listu!
                    globalExerciseList = excerciseList;
                    
                    // ... ostali kod za adapter i prikaz ...
                }
            }
            
            @Override
            public void onFailure(Call<List<Excercise>> call, Throwable t) {
                // ... kod za grešku ...
            }
        });
}
```

**Zašto izdvajamo u metodu?**
- Možemo pozvati `fetchExercises()` više puta bez dupliciranja koda
- Lakše održavanje i čitljivost
- Omogućava automatsko osvježavanje liste

---

### 3.2 Detekcija klika i navigacija

#### Korak 3: Dodajte `onResume()` za automatsko osvježavanje

```java
@Override
protected void onResume() {
    super.onResume();
    // Osvježi listu čim se vratimo na ovaj ekran!
    fetchExercises();
}
```

**Objašnjenje:** Metoda `onResume()` se poziva **svaki put** kada se aktivnost prikaže na ekranu, uključujući i povratak s drugog ekrana. Time osiguravamo da lista uvijek prikazuje najnovije podatke iz baze.

---

#### Korak 4: Postavite klik listener u `setupListeners()` metodi

Dodajte sljedeći kod **na kraj metode** `setupListeners()`:

```java
private void setupListeners() {
    logoutBtn.setOnClickListener(v -> {
        authManager.logout();
        startActivity(new Intent(this, LoginActivity.class));
    });

    addExcerciseBtn.setOnClickListener(v -> {
        startActivity(new Intent(this, ExcerciseActivity.class));
    });

    // DODAJTE OVO:
    // Postavljanje klika na stavku liste
    workoutListView.setOnItemClickListener((parent, view, position, id) -> {
        // Dohvaćamo odabranu vježbu iz globalne liste
        Excercise selected = globalExerciseList.get(position);
        
        // Kreiramo Intent za prelazak na EditExerciseActivity
        Intent intent = new Intent(DashboardActivity.this, EditExerciseActivity.class);
        
        // Šaljemo podatke na novi ekran
        intent.putExtra("id", String.valueOf(selected.getId()));
        intent.putExtra("name", selected.getName());
        intent.putExtra("desc", selected.getDescription());
        intent.putExtra("image", selected.getImageUrl());
        
        startActivity(intent);
    });
}
```

**Detaljna analiza koda:**

| Linija koda | Svrha i objašnjenje |
|-------------|---------------------|
| `workoutListView.setOnItemClickListener(...)` | Postavljanje slušatelja klika (Listener). Ova funkcija čeka da korisnik dodirne neki element unutar liste vježbi. |
| `Excercise selected = globalExerciseList.get(position);` | Kada se klik dogodi, koristimo parametar `position` (redni broj elementa na koji je kliknuto) da bismo dohvatili cijeli objekt vježbe iz memorije. |
| `Intent intent = new Intent(...)` | **Navigacija.** Kreiramo namjeru (Intent) da prijeđemo s trenutne aktivnosti na novu (EditExerciseActivity). |
| `intent.putExtra("id", ...)` | **Prijenos podataka.** ID vježbe je najvažniji podatak! Šaljemo ga na ekran za uređivanje. Bez ID-a, ne bismo znali koji zapis treba ažurirati ili obrisati u bazi. |
| `intent.putExtra("name", ...)` i ostali | Šaljemo i ostale podatke (naziv, opis, URL slike) kako bi polja na ekranu za uređivanje bila automatski popunjena trenutnim vrijednostima. |
| `startActivity(intent);` | Izvršavamo namjeru i prebacujemo korisnika na ekran za uređivanje. |

---

## 4. Logika uređivanja i slanje lokalnih notifikacija

Kreirajte **novu Java klasu** `EditExerciseActivity.java`.

**Lokacija:** `app/src/main/java/ba/sum/fsre/fitness/activities/EditExerciseActivity.java`

**Kako kreirati:**
1. Desni klik na `activities` paket
2. `New → Java Class`
3. Naziv: `EditExerciseActivity`

---

### 4.1 Struktura klase i varijable

```java
package ba.sum.fsre.fitness.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.List;

import ba.sum.fsre.fitness.R;
import ba.sum.fsre.fitness.models.Excercise;
import ba.sum.fsre.fitness.repository.ExerciseRepository;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditExerciseActivity extends AppCompatActivity {
    private EditText nameInput, descInput;
    private Button saveBtn, deleteBtn;
    private ExerciseRepository repository;

    private String exerciseId;
    private String currentImageUrl;
    private String currentExerciseName; // Pamtimo ime za notifikaciju
```

**Objašnjenje varijabli:**

| Varijabla | Tip | Svrha |
|-----------|-----|-------|
| `nameInput`, `descInput` | `EditText` | Polja za unos novog naziva i opisa |
| `saveBtn`, `deleteBtn` | `Button` | Gumbi za akcije (ažuriranje/brisanje) |
| `repository` | `ExerciseRepository` | Instanca klase za komunikaciju s API-jem |
| `exerciseId` | `String` | **KLJUČNO:** ID vježbe koju uređujemo (primljen preko Intent-a) |
| `currentImageUrl` | `String` | URL slike (potreban za update poziv) |
| `currentExerciseName` | `String` | Naziv vježbe (za prikaz u notifikaciji) |

---

### 4.2 Metoda za slanje notifikacije

```java
// Metoda za slanje notifikacije
private void sendDeletionNotification(String name) {
    String CHANNEL_ID = "fitness_channel";
    
    // Android 8.0+ zahtijeva NotificationChannel
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Obavijesti o vježbama",
                NotificationManager.IMPORTANCE_DEFAULT
        );
        channel.setDescription("Obavijesti o brisanju i ažuriranju vježbi");
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.createNotificationChannel(channel);
        }
    }

    NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Vježba obrisana")
            .setContentText("Vježba '" + name + "' je trajno uklonjena.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true);

    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
    
    // Provjera dozvole za Android 13+
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) 
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Nema dozvole za notifikacije", Toast.LENGTH_SHORT).show();
            return;
        }
    }
    
    try {
        notificationManager.notify(1, builder.build());
    } catch (SecurityException e) {
        Toast.makeText(this, "Greška prilikom slanja notifikacije", Toast.LENGTH_SHORT).show();
    }
}
```

**Objašnjenje ključnih dijelova:**

1. **NotificationChannel** (Android 8.0+): Od Androida 8.0, sve notifikacije moraju biti pridružene kanalu. Kanal omogućava korisniku da kontrolira prioritet i zvukove obavijesti.

2. **NotificationCompat.Builder**: Kreira strukturu notifikacije s naslovom, tekstom i ikonom.

3. **Provjera dozvole (Android 13+)**: Od Androida 13, aplikacija mora eksplicitno zatražiti dozvolu za slanje notifikacija.

---

### 4.3 Glavna logika - onCreate()

```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_edit_exercise);

    // 1. Povezivanje UI elemenata (XML -> Java)
    nameInput = findViewById(R.id.editName);
    descInput = findViewById(R.id.editDesc);
    saveBtn = findViewById(R.id.btnSave);
    deleteBtn = findViewById(R.id.btnDelete);

    // Inicijalizacija repozitorija
    repository = new ExerciseRepository(this);

    // 2. Prijem podataka iz prošle aktivnosti
    if (getIntent().getExtras() != null) {
        // Preuzimamo ključne podatke (ID je najvažniji!)
        exerciseId = getIntent().getStringExtra("id");
        currentImageUrl = getIntent().getStringExtra("image");
        currentExerciseName = getIntent().getStringExtra("name");

        // Popunjavamo polja starim podacima
        nameInput.setText(currentExerciseName);
        descInput.setText(getIntent().getStringExtra("desc"));
    }
```

**Što se ovdje događa:**

1. **findViewById**: Povezujemo Java varijable s elementima iz XML layout-a
2. **getIntent().getExtras()**: Primamo podatke koje je `DashboardActivity` poslala
3. **setText()**: Automatski popunjavamo polja trenutnim vrijednostima

---

### 4.4 Logika za SPREMANJE (UPDATE)

```java
    // 3. LOGIKA ZA SPREMANJE (UPDATE)
    saveBtn.setOnClickListener(v -> {
        String newName = nameInput.getText().toString().trim();
        String newDesc = descInput.getText().toString().trim();

        // Validacija
        if (newName.isEmpty()) {
            Toast.makeText(EditExerciseActivity.this, "Unesite naziv vježbe", Toast.LENGTH_SHORT).show();
            return;
        }

        repository.update(exerciseId, newName, newDesc, currentImageUrl).enqueue(new Callback<List<Excercise>>() {
            @Override
            public void onResponse(Call<List<Excercise>> call, Response<List<Excercise>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(EditExerciseActivity.this, "Ažurirano!", Toast.LENGTH_SHORT).show();
                    // finish() zatvara ekran i vraća nas na Dashboard
                    finish();
                } else {
                    Toast.makeText(EditExerciseActivity.this, "Greška pri ažuriranju: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Excercise>> call, Throwable t) {
                Toast.makeText(EditExerciseActivity.this, "Greška: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    });
```

**Analiza koraka:**

| Akcija u kodu | Objašnjenje |
|---------------|-------------|
| `saveBtn.setOnClickListener(...)` | Definiramo akciju kada korisnik dodirne gumb |
| `getText().toString().trim()` | Čitamo nove vrijednosti iz polja za unos i uklanjamo razmake |
| `if (newName.isEmpty())` | **Validacija:** Provjeravamo je li korisnik unio naziv |
| `repository.update(...)` | Asinkroni poziv API-ju. Šaljemo ID (koji ostaje isti) i nove podatke |
| `response.isSuccessful()` | Provjeravamo je li server uspješno obavio UPDATE |
| `finish()` | Zatvaramo trenutni ekran. Time se automatski poziva `onResume()` u DashboardActivity za osvježavanje liste |

---

### 4.5 Logika za BRISANJE (DELETE)

```java
    // 4. LOGIKA ZA BRISANJE (DELETE)
    deleteBtn.setOnClickListener(v -> {
        repository.delete(exerciseId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    // Šaljemo notifikaciju prije izlaska
                    sendDeletionNotification(currentExerciseName);
                    Toast.makeText(EditExerciseActivity.this, "Vježba obrisana!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(EditExerciseActivity.this, "Greška pri brisanju: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(EditExerciseActivity.this, "Greška: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    });
} // Kraj metode onCreate
```

**Analiza koraka:**

| Akcija u kodu | Objašnjenje |
|---------------|-------------|
| `repository.delete(exerciseId)` | Poziv API-ju za brisanje. Šaljemo samo ID vježbe |
| `sendDeletionNotification(...)` | Prije zatvaranja ekrana, šaljemo notifikaciju korisniku |
| `finish()` | Zatvaramo ekran i vraćamo se na Dashboard |

---

## 5. Registracija aktivnosti u AndroidManifest.xml

**Lokacija:** `app/src/main/AndroidManifest.xml`

Dodajte sljedeće **unutar** `<application>` taga (prije ili poslije drugih aktivnosti):

```xml
<activity
    android:name=".activities.EditExerciseActivity"
    android:exported="false" />
```

**Također, dodajte dozvolu za notifikacije na vrh fajla (van application taga):**

```xml
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```

**Objašnjenje:**
- `android:name`: Potpuna putanja do klase aktivnosti
- `android:exported="false"`: Ova aktivnost nije dostupna drugim aplikacijama (samo internom korištenju)
- `POST_NOTIFICATIONS`: Dozvola potrebna za slanje notifikacija na Android 13+

---

## 6. Testiranje aplikacije

### 6.1 Pokretanje projekta

1. Povežite fizički uređaj ili pokrenite Android emulator
2. Kliknite **Run** (zeleni trokut) u Android Studiju
3. Pričekajte instalaciju aplikacije

### 6.2 Scenarij testiranja - Ažuriranje vježbe

**Koraci:**
1. Prijavite se u aplikaciju
2. Na Dashboardu, kliknite na bilo koju vježbu u listi
3. Otvorit će se ekran "Uredi vježbu" s popunjenim podacima
4. Promijenite naziv ili opis
5. Kliknite **"Spremi promjene"**
6. Trebali biste vidjeti Toast poruku "Ažurirano!"
7. Vratit ćete se na Dashboard gdje će lista biti automatski osvježena s novim podacima

### 6.3 Scenarij testiranja - Brisanje vježbe

**Koraci:**
1. Kliknite na vježbu koju želite obrisati
2. Na ekranu za uređivanje, kliknite **"Obriši vježbu"** (crveni gumb)
3. Trebali biste vidjeti:
   - Toast poruku "Vježba obrisana!"
   - **Notifikaciju** u statusnoj traci
4. Vratit ćete se na Dashboard gdje ta vježba više neće biti prikazana

### 6.4 Mogući problemi i rješenja

| Problem | Uzrok | Rješenje |
|---------|-------|----------|
| Aplikacija crashuje nakon klika na vježbu | `EditExerciseActivity` nije registrirana u Manifestu | Provjerite AndroidManifest.xml - mora sadržavati `<activity>` tag |
| Lista se ne osvježava nakon izmjene | `onResume()` nije implementiran | Dodajte `onResume()` u DashboardActivity |
| Notifikacija se ne prikazuje | Nedostaje dozvola | Dodajte `POST_NOTIFICATIONS` u AndroidManifest.xml |
| Greška "Cannot resolve symbol 'EditExerciseActivity'" | Klasa ne postoji ili nije u pravilnom paketu | Provjerite da je klasa kreirana u `activities` paketu |

---

## 7. Ključne točke i sažetak vježbe

Ova vježba uspješno je zaokružila potpunu **CRUD funkcionalnost** aplikacije (Create, Read, Update, Delete) koristeći moderne Android prakse.

| Komponenta | Uloga | Mehanizam |
|------------|-------|-----------|
| XML Sučelje (`activity_edit_exercise`) | Dizajn unosa: Definirali smo ID-ove (editName, btnDelete itd.) ključne za povezivanje Jave s vizualnim elementima | Vizualni sloj (View) |
| `DashboardActivity.onResume()` | Automatsko osvježavanje liste: Osigurava da se lista vježbi ponovno dohvati s API-ja čim se korisnik vrati s ekrana za uređivanje/brisanje | Životni ciklus aktivnosti |
| `EditExerciseActivity.onCreate()` | Logika povezivanja: Preuzima ID vježbe, popunjava polja i postavlja "slušatelje klika" (onClickListener) za spremanje i brisanje | Prijem Intent podataka |
| API Metode (`repository.update/.delete`) | Komunikacija s bazom: Gumbi su povezani s asinkronim (Retrofit) pozivima koji šalju zahtjeve Supabase platformi | Asinkrono mrežno pozivanje |
| Lokalna notifikacija | Potvrda akcije: Nakon uspješnog brisanja, umjesto običnog Toast prikaza, šalje se profesionalna obavijest u statusnu traku | NotificationManager i NotificationChannel |

---

## 8. Dodatni izazovi (za napredne studente)

### Izazov 1: Dodajte dijalog za potvrdu brisanja
Umjesto direktnog brisanja, prikažite `AlertDialog` koji pita korisnika: "Jeste li sigurni da želite obrisati ovu vježbu?"

**Hint:**
```java
new AlertDialog.Builder(this)
    .setTitle("Potvrda")
    .setMessage("Jeste li sigurni da želite obrisati ovu vježbu?")
    .setPositiveButton("Da", (dialog, which) -> { /* kod za brisanje */ })
    .setNegativeButton("Ne", null)
    .show();
```

### Izazov 2: Omogućite uređivanje slike
Dodajte mogućnost odabira nove slike za vježbu prilikom uređivanja (slično kao kod kreiranja nove vježbe).

### Izazov 3: Animirajte brisanje stavke
Umjesto trenutnog nestanka, dodajte animaciju (swipe-to-delete) koristeći `ItemTouchHelper`.

---

## Zaključak

Uspješnom implementacijom ovih koraka, aplikacija sada nudi stabilan i dosljedan rad s podacima, što predstavlja temelj za daljnji profesionalni razvoj mobilnih aplikacija.

**Što smo naučili:**
- ✅ Komunikaciju između aktivnosti putem `Intent.putExtra()`
- ✅ Korištenje `onResume()` za osvježavanje podataka
- ✅ Implementaciju UPDATE i DELETE HTTP operacija
- ✅ Kreiranje i slanje lokalnih notifikacija
- ✅ Validaciju korisničkog unosa
- ✅ Rukovanje greškama i prikazivanje korisničkih poruka

**Sljedeći koraci:**
- Implementacija `RecyclerView` za bolju performansu liste
- Dodavanje offline podrške (Room baza podataka)
- Implementacija pull-to-refresh funkcionalnosti

---

**Autor:** [Vaše ime]  
**Datum:** [Datum]  
**Verzija:** 1.0

