package com.example.kelimebilmece;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

public class SplashScreenActivity extends AppCompatActivity {

    //Sorular için listeler
    private String[] sorularList = {"Mutfakta iş yaparken veya yemek yerken kullanılan aletler nelerdir?",
    "İç Anadolu bölgesindeki iller?", "Marmara bölgesindeki iller?", "En fazla il hangi bölgemizde bulunur?",
            "Bilgisayar markaları?", "Telefon markaları?"};
    private String[] sorularKodList = {"mutfakS1", "illerS1", "illerS2", "bölge1", "pc", "tel"};

    //Kelimeler için listeler
    private String[] kelimelerList = {"Çatal", "Kaşık", "Bıçak", "Tabak", "Tencere", "Tava", "Çaydanlık", "Süzgeç",

    "Aksaray", "Ankara", "Çankırı", "Eskişehir", "Karaman", "Kayseri", "Kırıkkale", "Kırşehir", "Konya", "Nevşehir",
            "Niğde", "Sivas", "Yozgat",

    "İstanbul", "Edirne", "Kırklareli", "Tekirdağ", "Çanakkale", "Kocaeli", "Yalova", "Sakarya", "Bilecik", "Bursa", "Balıkesir",

            "Karadeniz",

            "Lenovo", "Hp", "Dell", "Acer", "Asus", "Apple", "Razer", "Msı", "Samsung", "Huawei", "Casper", "Monster",
            "Toshiba", "Microsoft", "Honor", "Gigabyte",

            "Samsung", "Xiaomi", "Apple", "Oppo", "Huawei", "Lg", "Nokia", "Honor", "Htc", "Meizu", "OnePlus", "Tcl", "Asus", "Poco",
    };

    private String[] kelimelerKodList = {"mutfakS1", "mutfakS1", "mutfakS1", "mutfakS1", "mutfakS1", "mutfakS1", "mutfakS1",
            "mutfakS1",

            "illerS1", "illerS1", "illerS1", "illerS1", "illerS1", "illerS1", "illerS1", "illerS1", "illerS1", "illerS1",
            "illerS1", "illerS1", "illerS1",

            "illerS2", "illerS2", "illerS2", "illerS2", "illerS2", "illerS2", "illerS2", "illerS2", "illerS2", "illerS2", "illerS2",

            "bölge1",

            "pc", "pc", "pc", "pc", "pc", "pc", "pc", "pc", "pc", "pc", "pc", "pc", "pc", "pc", "pc", "pc",

            "tel", "tel", "tel", "tel", "tel", "tel", "tel", "tel", "tel", "tel", "tel", "tel", "tel", "tel",
    };


    private ProgressBar mProgress;
    private TextView mTextView;
    private SQLiteDatabase database;
    private float maxProgress = 100f, artacakProgress, progressMiktarı = 0;
    private Cursor cursor;
    static public HashMap<String, String> sorularHashMap;
    private String sqlSorgusu;
    private SQLiteStatement statement;
    private MediaPlayer gameTheme;

    private SharedPreferences preferences;
    private boolean muzikDurum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        mProgress = findViewById(R.id.splashActivity_progressBar);
        mTextView=findViewById(R.id.splashActivity_txtViewState);
        sorularHashMap = new HashMap<>();
        gameTheme = MediaPlayer.create(this, R.raw.music);
        //Müzik bittiğinde tekrar devam etmesini sağlıyor
        gameTheme.setLooping(true);

        preferences = this.getSharedPreferences("com.example.kelimebilmece", MODE_PRIVATE);
        muzikDurum = preferences.getBoolean("muzikDurumu", false);


        //Veri tabanının oluşturulması.   qKod UNIQUE kısmı benzersiz yapmak için kullanıldı
        try {
            database = this.openOrCreateDatabase("KelimeBilmece", MODE_PRIVATE, null);
            //Kullanıcı üyelik bilgisi veritabanı
            database.execSQL("CREATE TABLE IF NOT EXISTS Ayarlar (k_adi VARCHAR, k_heart VARCHAR, k_image BLOB)");
            //Ayarlar tablosundaki verileri getir
            cursor = database.rawQuery("SELECT * FROM Ayarlar", null);

            //kayıt olmadığı için kayıt yapma işlemi gerçekleşecek    İlk Kayıt
            if (cursor.getCount() < 1)
                database.execSQL("INSERT INTO Ayarlar (k_adi, k_heart) VALUES ('Oyuncu', '0')");


            database.execSQL("CREATE TABLE IF NOT EXISTS Sorular (id INTEGER PRIMARY KEY, sKod VARCHAR UNIQUE, soru VARCHAR)");
            database.execSQL("CREATE TABLE IF NOT EXISTS Kelimeler (kKod VARCHAR, kelime VARCHAR, FOREIGN KEY (kKod) REFERENCES Sorular (sKod)) ");

            //uygulama her açıldığında soruları tekrar tekrar yükleyeceği için verileri siliyoruz.
            database.execSQL("DELETE FROM Sorular");
            database.execSQL("DELETE FROM Kelimeler");

            //Metodları çağırıyoruz
            sqlSoruEkle();
            sqlKelimeEkle();

            //Progres barın dolum aşaması için veri tabanındaki soru sayısını 100 e böleceğiz
            //Veri miktarını alıyoruz
            cursor = database.rawQuery("SELECT * FROM Sorular", null);
            artacakProgress = maxProgress / cursor.getCount();

            //Verileri alıyoruz
            int sKodIndex = cursor.getColumnIndex("sKod");
            int soruIndex = cursor.getColumnIndex("soru");

            mTextView.setText("Sorular Yükleniyor...");

            //verileri eklemeye devam ettikçe yapılacaklar
            //sorularListesine ekleme yap, progresMiktarını artır.
            while (cursor.moveToNext()){
                sorularHashMap.put(cursor.getString(sKodIndex), cursor.getString(soruIndex));
                progressMiktarı += artacakProgress;
                mProgress.setProgress((int)progressMiktarı);
            }
            mTextView.setText("Uygulama Başlatılıyor...");
            cursor.close();

            //Uygulama başlatılıyor yazısından sonra 0.5 saniye beklet ve ana ekranı aç
            new CountDownTimer(500, 1000){
                @Override
                public void onTick(long l) {
                }
                @Override
                public void onFinish() {
                    Intent mainIntent = new Intent(SplashScreenActivity.this, MainActivity.class);
                    startActivity(mainIntent);
                }
            }.start();
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }


    private void sqlSoruEkle() {
            //Soruları oluşturduğumuz diziden ekleme işlemini yapıyruz
            try {
                for (int s = 0; s<sorularList.length; s++){
                    sqlSorgusu = "INSERT INTO Sorular (sKod, soru) VALUES (?, ?)";
                    statement = database.compileStatement(sqlSorgusu);
                    statement.bindString(1, sorularKodList[s]);
                    statement.bindString(2, sorularList[s]);
                    statement.execute();
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }
    }


    //Kelimeleri ekleme işlemi
    private void sqlKelimeEkle() {
        try {
            for (int k = 0; k<kelimelerList.length; k++){
                sqlSorgusu = "INSERT INTO Kelimeler (kKod, kelime) VALUES (?, ?)";
                statement = database.compileStatement(sqlSorgusu);
                statement.bindString(1, kelimelerKodList[k]);
                statement.bindString(2, kelimelerList[k]);
                statement.execute();
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (muzikDurum)
            gameTheme.start();
    }

}