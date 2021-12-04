package com.example.kelimebilmece;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class PlayActivity extends AppCompatActivity {

    private AlertDialog.Builder alert;

    private Intent get_intent;
    private int hakSayisi, sonHakSayisi;

    private SQLiteStatement statement;
    private String sqlSorgusu;

    private TextView textViewQuestion, textViewQuest, textViewHeartCount;
    private EditText editTextTahmin;
    private SQLiteDatabase database;
    private Cursor cursor;
    private ArrayList<String> sorularList;
    private ArrayList<String> sorularKodList;
    private ArrayList<String> kelimelerList;
    private ArrayList<Character> kelimeHarfleri;

    private Random rndSoru, rndKelime, rndHarf;
    private int rndSoruNumber, rndKelimeNumber, rndHarfNumber, rastgeleBelirlenecekHarfSayisi;
    private String rastgeleSoru, rastgeleSoruKodu, rastgeleKelime, kelimeBilgisi, textTahminDegeri;

    //İstatistik dialog
    private Dialog istatistikDialog;
    private TextView istatistikQuestion, istatistikWord, istatistikFalseGuess;
    private ProgressBar istatistikBarQuestion, istatistikBarWord, istatistikBarFalseGuess;
    private ImageView istatistikImgClose;
    private LinearLayout istatistikLinear;
    private Button istatistikBtnExit, istatistikBtnPlayAgain;
    private WindowManager.LayoutParams params;
    private int cozulenSoruSayisi = 0, cozulenKelimeSayisi = 0, yapilanYanlisSayisi = 0, maksSoruSayisi, maksKelimeSayisi;


    private AdView mAdview;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        mAdview = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdview.loadAd(adRequest);


        textViewQuestion = findViewById(R.id.playActivity_txtQuestion);
        textViewQuest = findViewById(R.id.playActivity_txtQuest);
        editTextTahmin = findViewById(R.id.playActivity_editTxtGuess);
        textViewHeartCount = findViewById(R.id.play_activity_txtViewUserHeartCount);

        sorularList = new ArrayList<>();
        sorularKodList = new ArrayList<>();
        kelimelerList = new ArrayList<>();
        rndSoru = new Random();
        rndKelime = new Random();
        rndHarf = new Random();


        get_intent = getIntent();
        hakSayisi = get_intent.getIntExtra("heartCount", 0);
        textViewHeartCount.setText("+"+ hakSayisi);

        //Tablodan değerleri almak için
        for (Map.Entry soru : SplashScreenActivity.sorularHashMap.entrySet()){
            sorularList.add(String.valueOf(soru.getValue()));
            sorularKodList.add(String.valueOf(soru.getKey()));
        }

        randomSoruGetir();
    }


    //Kullanıcı geri tuşuna basmış ise alert dialog göster çıkmak isteyip istmediğini sor
    @Override
    public void onBackPressed() {
        alert = new AlertDialog.Builder(this);
        alert.setTitle("Kelime Bilmece");
        alert.setMessage("Geri Dönmek İstediğinize Emin Misiniz?");
        alert.setIcon(R.mipmap.ic_kelime_bilmecee);
        alert.setPositiveButton("Hayır", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        alert.setNegativeButton("Evet", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mainIntent();
            }
        });
        alert.show();
    }



    public void btnHarfAl(View v) {
        if (hakSayisi > 0){
            rastgeleHarfAl();
            sonHakSayisi = hakSayisi;
            hakSayisi--;
            kalanHakkiKaydet(hakSayisi, sonHakSayisi);
        }
        else
            Toast.makeText(getApplicationContext(), "Can Sayisi Yetersiz", Toast.LENGTH_SHORT).show();
    }



    private void kalanHakkiKaydet(int hSayisi, int sonHSayisi){
        try {
            sqlSorgusu = "UPDATE Ayarlar SET k_heart = ? WHERE k_heart = ?";
            statement = database.compileStatement(sqlSorgusu);
            statement.bindString(1, String.valueOf(hSayisi));
            statement.bindString(2, String.valueOf(sonHSayisi));
            statement.execute();

            textViewHeartCount.setText("+"+ hSayisi);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }



    public void btnTahminEt(View v) {
        textTahminDegeri = editTextTahmin.getText().toString();

        //Tahmin değeri boş değilse girilen kelime ile doğru kelimeyi karşılaştır
        if (!TextUtils.isEmpty(textTahminDegeri)){
            //Doğru Tahmin
            //toLowerCase ile küçük-büyük değer farketmeksizin kontrolünü sağladık
            if (textTahminDegeri.toLowerCase().contains(rastgeleKelime.toLowerCase())){
                editTextTahmin.setText("");
                cozulenKelimeSayisi++;

                //KelimelerListesinde kelime varsa kelimeyi ekrana yaz yoksa soru değiştir, soru bittiyse istatistiği ekranda göster
                if (kelimelerList.size() > 0)
                    randomKelimeGetir();
                else {
                    if (sorularList.size() > 0) {
                        cozulenSoruSayisi++;
                        randomSoruGetir();
                    }
                    //İstatiskik hazırlanıp ekrana yazdırılacak  & Sorular Bitti
                    else
                        maksVerileriHesapla("oyunBitti");
                }

            }
            //Yanlış Tahmin    hak sayisi işlemleri  &&  dialog ekrana yazdırılması
            else {
                if (hakSayisi > 0){
                    sonHakSayisi = hakSayisi;
                    hakSayisi--;
                    kalanHakkiKaydet(hakSayisi, sonHakSayisi);
                }
                else{
                    //Alert Dialog gelecek; can kalmadı oyun bitti vb yazı yazacak, Ana menüye dönüş tuşu bırakılacak
                    yapilanYanlisSayisi++;
                    maksVerileriHesapla("oyunBitti");
                }

            }
        }
        else
            Toast.makeText(PlayActivity.this, "Tahmin Değeri Boş Bırakılamaz!", Toast.LENGTH_SHORT).show();
    }



    private void rastgeleHarfAl() {
        //ilk olarak kelimeHarfleri dizisinde eleman olup olmadığının kontrolü yapılıyor
        if (kelimeHarfleri.size() > 0){
            rndHarfNumber = rndHarf.nextInt(kelimeHarfleri.size());
            //TextViewin o an içerisinde bulunan değerleri içindeki boşlukları silerek aldık
            String[] txtHarfler = textViewQuest.getText().toString().split(" ");
            char[] gelenKelimeHarfler = rastgeleKelime.toCharArray();

            //Harflerin alt çizgilere yazdırılması kontrolleri
            for (int i = 0; i < rastgeleKelime.length(); i++){
                if (txtHarfler[i].equals("_") && gelenKelimeHarfler[i] == kelimeHarfleri.get(rndHarfNumber)){
                    txtHarfler[i] = String.valueOf(kelimeHarfleri.get(rndHarfNumber));
                    kelimeBilgisi = "";

                    for (int j = 0; j < txtHarfler.length; j++){
                        if (j < txtHarfler.length - 1)
                            kelimeBilgisi += txtHarfler[j] + " ";
                        else
                            kelimeBilgisi += txtHarfler[j];
                    }
                    break;
                }
            }

            textViewQuest.setText(kelimeBilgisi);

            kelimeHarfleri.remove(rndHarfNumber);

        }
        //Alacak harf kalmadıysa ekrana hata mesajı yazdırılıyor
        else
            Toast.makeText(PlayActivity.this, "Alacak Başka Harf Kalmadı!", Toast.LENGTH_SHORT).show();
    }



    private void randomSoruGetir() {
        //Rastgele değer alıp yazdırabilmek için
        rndSoruNumber = rndSoru.nextInt(sorularList.size());
        //Rastgele gelen soruyu ve kodunu alıyoruz
        rastgeleSoru = sorularList.get(rndSoruNumber);
        rastgeleSoruKodu = sorularKodList.get(rndSoruNumber);

        //Temizliyoruz
        sorularList.remove(rndSoruNumber);
        sorularKodList.remove(rndSoruNumber);

        //rastgele gelen soruyu ekrana yazdırıyoruz
        textViewQuestion.setText(rastgeleSoru);

        //databaseyi açtık
        try {
            database = this.openOrCreateDatabase("KelimeBilmece", MODE_PRIVATE, null);
            //random seçilen soruya karşılık gelen kelimelerin alınması     "?" sonra yazılan kısım ? yerine geçmiş oluyor, o değere göre verileri alıyor
            cursor = database.rawQuery("SELECT * FROM Kelimeler WHERE kKod = ?", new String[]{rastgeleSoruKodu});

            int kelimeIndex = cursor.getColumnIndex("kelime");

            //Kelimelerin diziye atılması
            while (cursor.moveToNext()){
                kelimelerList.add(cursor.getString(kelimeIndex));
            }
            cursor.close();

        }
        catch (Exception e){
            e.printStackTrace();
        }
        randomKelimeGetir();
    }



    private void randomKelimeGetir() {
        kelimeBilgisi = "";

        //random seçilen sorunun kelimelirini attığımız diziden random kelime çekiyoruz
        rndKelimeNumber = rndKelime.nextInt(kelimelerList.size());
        //Rastgele gelen kelimeyi alıyoruz
        rastgeleKelime = kelimelerList.get(rndKelimeNumber);
        //çıkan kelimeyi siliyoruz ki bir daha ekrana çıkmasın
        kelimelerList.remove(rndKelimeNumber);

        //Alt çizgilere gelecek harflerin ayarlanması
        for (int i = 0; i < rastgeleKelime.length(); i++){
            if (i < rastgeleKelime.length()-1){
                kelimeBilgisi += "_ ";
            }
            else{
                kelimeBilgisi += "_";
            }
        }
        textViewQuest.setText(kelimeBilgisi);

        System.out.println("Cevap: "+rastgeleKelime);
        System.out.println("Harf Sayısı: "+ rastgeleKelime.length());


        kelimeHarfleri = new ArrayList<>();

        //Kelimenin harflerini parçalayıp kelimeharfleri arrayina atıyoruz
        for (char harf : rastgeleKelime.toCharArray()){
            kelimeHarfleri.add(harf);
        }

        if (rastgeleKelime.length() >= 3 && rastgeleKelime.length() <= 6)
            rastgeleBelirlenecekHarfSayisi = 1;

        else if (rastgeleKelime.length() > 6 && rastgeleKelime.length() <= 9)
            rastgeleBelirlenecekHarfSayisi = 2;

        else if (rastgeleKelime.length() > 9 && rastgeleKelime.length() <= 12)
            rastgeleBelirlenecekHarfSayisi = 3;

        else if (rastgeleKelime.length() > 12 && rastgeleKelime.length() <= 14)
            rastgeleBelirlenecekHarfSayisi = 4;

        else if (rastgeleKelime.length() > 14)
            rastgeleBelirlenecekHarfSayisi = 5;

        else
            rastgeleBelirlenecekHarfSayisi = 0;

        for (int i = 0; i < rastgeleBelirlenecekHarfSayisi; i++){
            rastgeleHarfAl();
        }
    }



    private void mainIntent() {
        Intent mainIntent = new Intent(this, MainActivity.class);
        finish();
        startActivity(mainIntent);
        //Geri tuşuna basıldığında playActivity yukarı gidecek mainActivity aşağı gelecek
        overridePendingTransition(R.anim.slide_out_up, R.anim.slide_in_down);
    }


    public void btnIstatistikTablosu(View v) {
        maksVerileriHesapla("");
    }


    private void istatistikTablosunuGöster(@NonNull String oyunDurumu, int maksSoruSayisi, int maksKelimeSayisi, int cozulenSoruSayisi, int cozulenKelimeSayisi, int yapilanYanlisSayisi) {
        istatistikDialog = new Dialog(this);
        params = new WindowManager.LayoutParams();
        params.copyFrom(istatistikDialog.getWindow().getAttributes());
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        istatistikDialog.setContentView(R.layout.custom_dialog_statistic_table);

        istatistikImgClose = (ImageView) istatistikDialog.findViewById(R.id.custom_dialog_statistic_table_imageViewClose);
        istatistikLinear = (LinearLayout) istatistikDialog.findViewById(R.id.custom_dialog_statistic_table_linearLayout);

        istatistikBtnExit = (Button) istatistikDialog.findViewById(R.id.custom_dialog_statistic_table_btnMainMenu);
        istatistikBtnPlayAgain = (Button) istatistikDialog.findViewById(R.id.custom_dialog_statistic_table_btnPlayAgain);

        istatistikQuestion = (TextView) istatistikDialog.findViewById(R.id.custom_dialog_statistic_table_textViewQuesitonCount);
        istatistikWord = (TextView) istatistikDialog.findViewById(R.id.custom_dialog_statistic_table_textViewWordCount);
        istatistikFalseGuess = (TextView) istatistikDialog.findViewById(R.id.custom_dialog_statistic_table_textViewFalseGuessCount);

        istatistikBarQuestion = (ProgressBar) istatistikDialog.findViewById(R.id.custom_dialog_statistic_table_progressBarQuesitonCount);
        istatistikBarWord = (ProgressBar) istatistikDialog.findViewById(R.id.custom_dialog_statistic_table_progressBarWordCount);
        istatistikBarFalseGuess = (ProgressBar) istatistikDialog.findViewById(R.id.custom_dialog_statistic_table_progressBarFalseGuessCount);

        //oyun bitmiş ise yani soru kalmamışsa ekrana dialogu getir ve butonlarda aktif olsun
        if (oyunDurumu.matches("oyunBitti")){
             istatistikDialog.setCancelable(false);
             istatistikLinear.setVisibility(View.VISIBLE);
            istatistikImgClose.setVisibility(View.INVISIBLE);
        }

        istatistikQuestion.setText(cozulenSoruSayisi + "/" + maksSoruSayisi);
        istatistikWord.setText(cozulenKelimeSayisi + "/" + maksKelimeSayisi);
        istatistikFalseGuess.setText(yapilanYanlisSayisi + "/" + maksKelimeSayisi);

        istatistikBarQuestion.setProgress(cozulenSoruSayisi);
        istatistikBarWord.setProgress(cozulenKelimeSayisi);
        istatistikBarFalseGuess.setProgress(yapilanYanlisSayisi);

        istatistikImgClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                istatistikDialog.dismiss();
            }
        });

        istatistikBtnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mainIntent();
            }
        });

        istatistikBtnPlayAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Aktiviteyi tekrar çağırdık ve başlattık
                Intent thisIntent = new Intent(PlayActivity.this, PlayActivity.class);
                thisIntent.putExtra("heartCount", Integer.valueOf(textViewHeartCount.getText().toString()));
                finish();
                startActivity(thisIntent);
            }
        });

        istatistikDialog.getWindow().setAttributes(params);
        istatistikDialog.show();
    }


    private void maksVerileriHesapla(String oyunDurumu){
        try {
            //kKod ve sKod eşit olan durumların sayısını al
            cursor = database.rawQuery("SELECT * FROM Kelimeler, Sorular WHERE Kelimeler.kKod = Sorular.sKod", null);
            maksKelimeSayisi = cursor.getCount();

            //soru sayısını al
            cursor = database.rawQuery("SELECT * FROM Sorular", null);
            maksSoruSayisi = cursor.getCount();

            cursor.close();

            istatistikTablosunuGöster(oyunDurumu, maksSoruSayisi, maksKelimeSayisi, cozulenSoruSayisi, cozulenKelimeSayisi, yapilanYanlisSayisi);

        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

}