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

    //─░statistik dialog
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

        //Tablodan de─čerleri almak i├žin
        for (Map.Entry soru : SplashScreenActivity.sorularHashMap.entrySet()){
            sorularList.add(String.valueOf(soru.getValue()));
            sorularKodList.add(String.valueOf(soru.getKey()));
        }

        randomSoruGetir();
    }


    //Kullan─▒c─▒ geri tu┼čuna basm─▒┼č ise alert dialog g├Âster ├ž─▒kmak isteyip istmedi─čini sor
    @Override
    public void onBackPressed() {
        alert = new AlertDialog.Builder(this);
        alert.setTitle("Kelime Bilmece");
        alert.setMessage("Geri D├Ânmek ─░stedi─činize Emin Misiniz?");
        alert.setIcon(R.mipmap.ic_kelime_bilmecee);
        alert.setPositiveButton("Hay─▒r", new DialogInterface.OnClickListener() {
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

        //Tahmin de─čeri bo┼č de─čilse girilen kelime ile do─čru kelimeyi kar┼č─▒la┼čt─▒r
        if (!TextUtils.isEmpty(textTahminDegeri)){
            //Do─čru Tahmin
            //toLowerCase ile k├╝├ž├╝k-b├╝y├╝k de─čer farketmeksizin kontrol├╝n├╝ sa─člad─▒k
            if (textTahminDegeri.toLowerCase().contains(rastgeleKelime.toLowerCase())){
                editTextTahmin.setText("");
                cozulenKelimeSayisi++;

                //KelimelerListesinde kelime varsa kelimeyi ekrana yaz yoksa soru de─či┼čtir, soru bittiyse istatisti─či ekranda g├Âster
                if (kelimelerList.size() > 0)
                    randomKelimeGetir();
                else {
                    if (sorularList.size() > 0) {
                        cozulenSoruSayisi++;
                        randomSoruGetir();
                    }
                    //─░statiskik haz─▒rlan─▒p ekrana yazd─▒r─▒lacak  & Sorular Bitti
                    else
                        maksVerileriHesapla("oyunBitti");
                }

            }
            //Yanl─▒┼č Tahmin    hak sayisi i┼člemleri  &&  dialog ekrana yazd─▒r─▒lmas─▒
            else {
                if (hakSayisi > 0){
                    sonHakSayisi = hakSayisi;
                    hakSayisi--;
                    kalanHakkiKaydet(hakSayisi, sonHakSayisi);
                }
                else{
                    //Alert Dialog gelecek; can kalmad─▒ oyun bitti vb yaz─▒ yazacak, Ana men├╝ye d├Ân├╝┼č tu┼ču b─▒rak─▒lacak
                    yapilanYanlisSayisi++;
                    maksVerileriHesapla("oyunBitti");
                }

            }
        }
        else
            Toast.makeText(PlayActivity.this, "Tahmin De─čeri Bo┼č B─▒rak─▒lamaz!", Toast.LENGTH_SHORT).show();
    }



    private void rastgeleHarfAl() {
        //ilk olarak kelimeHarfleri dizisinde eleman olup olmad─▒─č─▒n─▒n kontrol├╝ yap─▒l─▒yor
        if (kelimeHarfleri.size() > 0){
            rndHarfNumber = rndHarf.nextInt(kelimeHarfleri.size());
            //TextViewin o an i├žerisinde bulunan de─čerleri i├žindeki bo┼čluklar─▒ silerek ald─▒k
            String[] txtHarfler = textViewQuest.getText().toString().split(" ");
            char[] gelenKelimeHarfler = rastgeleKelime.toCharArray();

            //Harflerin alt ├žizgilere yazd─▒r─▒lmas─▒ kontrolleri
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
        //Alacak harf kalmad─▒ysa ekrana hata mesaj─▒ yazd─▒r─▒l─▒yor
        else
            Toast.makeText(PlayActivity.this, "Alacak Ba┼čka Harf Kalmad─▒!", Toast.LENGTH_SHORT).show();
    }



    private void randomSoruGetir() {
        //Rastgele de─čer al─▒p yazd─▒rabilmek i├žin
        rndSoruNumber = rndSoru.nextInt(sorularList.size());
        //Rastgele gelen soruyu ve kodunu al─▒yoruz
        rastgeleSoru = sorularList.get(rndSoruNumber);
        rastgeleSoruKodu = sorularKodList.get(rndSoruNumber);

        //Temizliyoruz
        sorularList.remove(rndSoruNumber);
        sorularKodList.remove(rndSoruNumber);

        //rastgele gelen soruyu ekrana yazd─▒r─▒yoruz
        textViewQuestion.setText(rastgeleSoru);

        //databaseyi a├žt─▒k
        try {
            database = this.openOrCreateDatabase("KelimeBilmece", MODE_PRIVATE, null);
            //random se├žilen soruya kar┼č─▒l─▒k gelen kelimelerin al─▒nmas─▒     "?" sonra yaz─▒lan k─▒s─▒m ? yerine ge├žmi┼č oluyor, o de─čere g├Âre verileri al─▒yor
            cursor = database.rawQuery("SELECT * FROM Kelimeler WHERE kKod = ?", new String[]{rastgeleSoruKodu});

            int kelimeIndex = cursor.getColumnIndex("kelime");

            //Kelimelerin diziye at─▒lmas─▒
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

        //random se├žilen sorunun kelimelirini att─▒─č─▒m─▒z diziden random kelime ├žekiyoruz
        rndKelimeNumber = rndKelime.nextInt(kelimelerList.size());
        //Rastgele gelen kelimeyi al─▒yoruz
        rastgeleKelime = kelimelerList.get(rndKelimeNumber);
        //├ž─▒kan kelimeyi siliyoruz ki bir daha ekrana ├ž─▒kmas─▒n
        kelimelerList.remove(rndKelimeNumber);

        //Alt ├žizgilere gelecek harflerin ayarlanmas─▒
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
        System.out.println("Harf Say─▒s─▒: "+ rastgeleKelime.length());


        kelimeHarfleri = new ArrayList<>();

        //Kelimenin harflerini par├žalay─▒p kelimeharfleri arrayina at─▒yoruz
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
        //Geri tu┼čuna bas─▒ld─▒─č─▒nda playActivity yukar─▒ gidecek mainActivity a┼ča─č─▒ gelecek
        overridePendingTransition(R.anim.slide_out_up, R.anim.slide_in_down);
    }


    public void btnIstatistikTablosu(View v) {
        maksVerileriHesapla("");
    }


    private void istatistikTablosunuG├Âster(@NonNull String oyunDurumu, int maksSoruSayisi, int maksKelimeSayisi, int cozulenSoruSayisi, int cozulenKelimeSayisi, int yapilanYanlisSayisi) {
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

        //oyun bitmi┼č ise yani soru kalmam─▒┼čsa ekrana dialogu getir ve butonlarda aktif olsun
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
                //Aktiviteyi tekrar ├ža─č─▒rd─▒k ve ba┼člatt─▒k
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
            //kKod ve sKod e┼čit olan durumlar─▒n say─▒s─▒n─▒ al
            cursor = database.rawQuery("SELECT * FROM Kelimeler, Sorular WHERE Kelimeler.kKod = Sorular.sKod", null);
            maksKelimeSayisi = cursor.getCount();

            //soru say─▒s─▒n─▒ al
            cursor = database.rawQuery("SELECT * FROM Sorular", null);
            maksSoruSayisi = cursor.getCount();

            cursor.close();

            istatistikTablosunuG├Âster(oyunDurumu, maksSoruSayisi, maksKelimeSayisi, cozulenSoruSayisi, cozulenKelimeSayisi, yapilanYanlisSayisi);

        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

}