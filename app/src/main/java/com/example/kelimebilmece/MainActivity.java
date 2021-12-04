package com.example.kelimebilmece;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.OnPaidEventListener;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.ResponseInfo;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.rewarded.OnAdMetadataChangedListener;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.gms.ads.rewarded.ServerSideVerificationOptions;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements PurchasesUpdatedListener {

    private SQLiteDatabase database;
    private SQLiteStatement statement;
    private Cursor cursor;
    private String sqlSorgusu;
    private TextView txtUserHeartCount, txtViewUserName;
    private int heartIndex, nameIndex, heartCount, imgHeartDuration = 2000, sonCanDurumu;
    private RewardedAd mRewardedAd;
    private ConstraintLayout constra;
    private ImageView imgHeart, imgHeartDesign;
    private Bitmap imgHeartbitmap;
    private ConstraintLayout.LayoutParams heartParams;
    private Float imgHeartXPos, imgHeartYPos;
    private ObjectAnimator objectAnimatorHeartX, objectAnimatorHeartY;
    private AnimatorSet imgHeartAnimatorSet;
    private AdView mAdview;

    private WindowManager.LayoutParams params;

    //Settings Dialog
    private Dialog settingsDialog;
    private ImageView settingsImageClose;
    private Button settingsBtnName, settingsBtnProfile;
    private RadioButton settingsRadioOpen, settingsRadioClose;


    //ChangeName Dialog
    private Dialog changeNameDialog;
    private ImageView changeNameImgClose;
    private Button changeNameDialogbtn;
    private EditText changeNameEditTxtName;
    private String getchangeName;

    //Profil Resmi
    private int izinVer=0, izinVerildi=1;
    private Uri resimUri;
    private Bitmap resimBitmap;
    private CircleImageView userProfileImage;
    private ImageDecoder.Source resimDosyasi;
    private AlertDialog.Builder alertBuilder;
    private byte[] resimByte;
    private int resimIndex;

    //Müzik
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private boolean muzikDurum;


    private Dialog getHeartDialog;
    private ImageView getHeartImgClose, getHeartShowAndGet, getHeartBuyAndGet;

    //Mağaza
    private Dialog shopDialog;
    private ImageView shopDialogImgClose;
    private RecyclerView shopDialogRecyclerView;
    private ShopAdapter adapter;
    private GridLayoutManager manager;

    private BillingClient mBillingClient;
    private ArrayList<String> skuList;
    private ArrayList<Integer> heartList;
    private int heartPos;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        mAdview = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdview.loadAd(adRequest);

        //Reklam yüklenince görünürlüğü aç
        mAdview.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                mAdview.setVisibility(View.VISIBLE);
            }
        });

        reklamOlustur();


        txtUserHeartCount = findViewById(R.id.main_activity_txtViewUserHeartCount);
        constra = findViewById(R.id.main_activity_constra);
        imgHeartDesign = (ImageView) findViewById(R.id.main_activity_imgViewHeartDesign);
        txtViewUserName = findViewById(R.id.main_activity_textViewUserName);

        userProfileImage = findViewById(R.id.main_activity_circleImageViewProfile);

        //Müzik açma için baştaki ayar, true değer döndürüyor ki müzik çalmaya devam etsin
        preferences = this.getSharedPreferences("com.example.kelimebilmece", MODE_PRIVATE);
        muzikDurum = preferences.getBoolean("muzikDurumu", false);

        skuList = new ArrayList<>();
        heartList = new ArrayList<>();

        //Her satın alma işleminin idsi ayrı olduğu için ayarlama işlemlerini gerçekleştiriyoruz
        skuList.add("buy_heart1");
        skuList.add("buy_heart2");
        skuList.add("buy_heart3");
        skuList.add("buy_heart4");
        skuList.add("buy_heart5");

        //Aldığı canın adetini tutacak list
        heartList.add(5);
        heartList.add(20);
        heartList.add(50);
        heartList.add(150);
        heartList.add(500);


        //Market alışverişi
        mBillingClient = BillingClient.newBuilder(this)
                .setListener(this)
                //Satın alma işleminin kabul olması ile ilgili
                .enablePendingPurchases()
                .build();
        mBillingClient.startConnection(new BillingClientStateListener() {
            //Bağlantı yok
            @Override
            public void onBillingServiceDisconnected() {
                Toast.makeText(getApplicationContext(), "Ödeme Sistemi Geçerli Değil.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                //Hesapla ilgili bir sorun varsa çalışacak kısım
                if (billingResult.getResponseCode() != BillingClient.BillingResponseCode.OK)
                    Toast.makeText(getApplicationContext(), "Play Store Hesabınızı Kontrol Ediniz!", Toast.LENGTH_SHORT).show();
            }
        });


        //Kalp sayısının ayarlanması
        try {
            database = this.openOrCreateDatabase("KelimeBilmece", MODE_PRIVATE, null);
            cursor = database.rawQuery("SELECT * FROM Ayarlar", null);

            heartIndex = cursor.getColumnIndex("k_heart");
            nameIndex = cursor.getColumnIndex("k_adi");
            resimIndex = cursor.getColumnIndex("k_image");
            cursor.moveToFirst();


            resimByte = cursor.getBlob(resimIndex);
            if (resimByte != null){
                //Resim byte olarak geldi öncelikle bitmap haline dönüştürülecek
                resimBitmap = BitmapFactory.decodeByteArray(resimByte, 0, resimByte.length);
                //Circle içerisine atılıyor
                userProfileImage.setImageBitmap(resimBitmap);
            }


            //Veri alınıyor
            heartCount = Integer.valueOf(cursor.getString(heartIndex));
            txtUserHeartCount.setText("+"+ heartCount);
            txtViewUserName.setText(cursor.getString(nameIndex));

            cursor.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }


        //Kalp kazanınca oluşacak animasyonun resmini aktarıyoruz
        //resmi bitmapde tutuyoruz
        imgHeart = new ImageView(MainActivity.this);
        imgHeartbitmap = BitmapFactory.decodeResource(MainActivity.this.getResources(), R.drawable.heart);
        imgHeart.setImageBitmap(imgHeartbitmap);

        //ConstraintLayoutun kendi özelliğini kullanarak fotoğrafın boyutunu belirliyoruz
        heartParams = new ConstraintLayout.LayoutParams(96, 96);
        //Animasyonun nerden başlayacağını belirliyorum
        imgHeart.setLayoutParams(heartParams);
        //İlk konumunu giriyoruz
        imgHeart.setX(0);
        imgHeart.setY(0);
        //İlk konuumda görünmesini istemiyoruz
        imgHeart.setVisibility(View.INVISIBLE);
        //imgHeart'i activity'e ekliyoruz
        constra.addView(imgHeart);

    }


    public void mainBtnClick(View v) {
        switch (v.getId()){
            case R.id.main_activity_btnPlay:
                Intent playIntent = new Intent(this, PlayActivity.class);
                finish();
                playIntent.putExtra("heartCount", heartCount);
                startActivity(playIntent);
                //Sayfanın aşağıdan yukarıya gidip yeni açılacak sayfanın yukarıdan aşağıya inmesini sağlayacak olan animasyonun import edilmesi
                overridePendingTransition(R.anim.slide_out_up, R.anim.slide_in_down);
                break;

            case R.id.main_activity_btnShop:
                marketDialog();
                break;

            case R.id.main_activity_btnExit:
                onBackPressed();
                break;
        }

    }


    @Override
    public void onBackPressed() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Kelime Bilmece");
        alert.setIcon(R.mipmap.ic_kelime_bilmecee);
        alert.setMessage("Uygulamadan Çıkmak İstediğinize Emin Misiniz?");
        alert.setPositiveButton("Hayır", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        alert.setNegativeButton("Evet", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                moveTaskToBack(true);
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(0);
            }
        });
        alert.show();
    }


    public void btnHakKazan(View v) {
        canKazanmaMenusu();
    }


    private void reklamOlustur() {

        AdRequest adRequest1 = new AdRequest.Builder().build();

        RewardedAd.load(this, "ca-app-pub-2396996479721834/2416334298",
                adRequest1, new RewardedAdLoadCallback() {
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error.
                        Log.d(TAG, loadAdError.getMessage());
                        mRewardedAd = null;
                        reklamYukle();
                    }

                    @Override
                    public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                        mRewardedAd = rewardedAd;
                        Log.d(TAG, "Ad was loaded.");
                    }
                });
    }


    private void reklamYukle() {
        //Video izleme başarılı ise yapıalcaklar
        if (mRewardedAd != null) {
            Activity activityContext = MainActivity.this;
            mRewardedAd.show(activityContext, new OnUserEarnedRewardListener() {
                @Override
                public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                    // Handle the reward.
                    Log.d(TAG, "The user earned the reward.");
                    int rewardAmount = rewardItem.getAmount();
                    String rewardType = rewardItem.getType();

                    //sayfanın x pozisyonuna göre ortasını al
                    imgHeart.setX(constra.getPivotX());
                    //sayfanın y pozisyonuna göre ortasını al
                    imgHeart.setY(constra.getPivotY());
                    //Yukarıda kapattığımız görünürlüğü açıyoruz
                    imgHeart.setVisibility(View.VISIBLE);

                    //imgHeartDesign.getX() = imgHeartDesig'in x pozisyonuna göre kenarına hizala uzunluğunun yarısını bulup kenar köşe ile topla
                    imgHeartXPos = (imgHeartDesign.getX() + (imgHeartDesign.getWidth()/2f - 48));
                    imgHeartYPos = (imgHeartDesign.getY() + (imgHeartDesign.getHeight()/2f - 48));


                    //Kalp animasyonunun ayarlanması
                    objectAnimatorHeartX = ObjectAnimator.ofFloat(imgHeart, "x", imgHeartXPos);
                    objectAnimatorHeartX.setDuration(imgHeartDuration);

                    objectAnimatorHeartY = ObjectAnimator.ofFloat(imgHeart, "y", imgHeartYPos);
                    objectAnimatorHeartY.setDuration(imgHeartDuration);

                    imgHeartAnimatorSet = new AnimatorSet();
                    imgHeartAnimatorSet.playTogether(objectAnimatorHeartX);
                    imgHeartAnimatorSet.playTogether(objectAnimatorHeartY);
                    imgHeartAnimatorSet.start();
                    objectAnimatorHeartY.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animator) {

                        }

                        //Animasyon sonlanınca (Gitmesi gereken yere gidince) görünmez olacak
                        @Override
                        public void onAnimationEnd(Animator animator) {
                            imgHeart.setVisibility(View.INVISIBLE);
                            Toast.makeText(getApplicationContext(), "Can kazandın", Toast.LENGTH_SHORT).show();

                            //sonCanDurumu heartCount'a(o an ki cana) eşitledik, heartCount 1 can eklendi ve güncelleme işlemi yapıldı
                            sonCanDurumu = heartCount;
                            heartCount++;
                            canMiktariniGuncelle(sonCanDurumu, heartCount);
                        }

                        @Override
                        public void onAnimationCancel(Animator animator) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animator) {

                        }
                    });

                }
            });
        }
        //Video izleme başarılı değil ise
        else {
            Log.d(TAG, "The rewarded ad wasn't ready yet.");
        }
    }


    //Video izlendikten sonra can miktarının artırılması olayı
    //sonCanSayisi = güncellenmeden önceki can sayısı (Where kısmından sonra gelen)
    //canSayisi = güncellendikten sonraki can sayısı (Set kısmından sonra gelen)
    private void canMiktariniGuncelle(int sonCanSayisi, int canSayisi){
        try {
            sqlSorgusu = "UPDATE Ayarlar SET k_heart = ? WHERE k_heart = ?";
            statement = database.compileStatement(sqlSorgusu);
            statement.bindString(1, String.valueOf(canSayisi));
            statement.bindString(2, String.valueOf(sonCanSayisi));
            statement.execute();
             txtUserHeartCount.setText("+" + canSayisi);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }


    private void marketDialog(){
        shopDialog = new Dialog(this);
        params = new WindowManager.LayoutParams();
        params.copyFrom(shopDialog.getWindow().getAttributes());
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        shopDialog.setCancelable(false);
        shopDialog.setContentView(R.layout.custom_dialog_shop);

        shopDialogImgClose = (ImageView)shopDialog.findViewById(R.id.custom_dialog_shop_imageViewClose);
        shopDialogRecyclerView = (RecyclerView)shopDialog.findViewById(R.id.custom_dialog_shop_recyclerView);


        shopDialogImgClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shopDialog.dismiss();
            }
        });

        adapter = new ShopAdapter(Shop.getData(), this);

        shopDialogRecyclerView.setHasFixedSize(true);
        manager = new GridLayoutManager(this, 3);
        shopDialogRecyclerView.setLayoutManager(manager);
        //3 Kolon sayısı, 5 boşluk sayısı
        shopDialogRecyclerView.addItemDecoration(new GridItemDecoration(3, 5));

        shopDialogRecyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new ShopAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(Shop mShop, final int pos) {
                if (mBillingClient.isReady()){
                    SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
                    //Collection ile kalp satın işlemi için hangi butona basılmışsa ona göre satın alma işlemi gerçekleştirecek
                    //Uygulama için satın alma işlemi olacağı için INAPP seçtik
                    params.setSkusList(Collections.singletonList(skuList.get(pos))).setType(BillingClient.SkuType.INAPP);
                    mBillingClient.querySkuDetailsAsync(params.build(), new SkuDetailsResponseListener() {
                        @Override
                        public void onSkuDetailsResponse(@NonNull BillingResult billingResult, @Nullable List<SkuDetails> list) {
                            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && list != null){
                                BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                                        .setSkuDetails(list.get(0))
                                        .build();

                                mBillingClient.launchBillingFlow(MainActivity.this, flowParams);

                                heartPos = pos;
                            }
                        }
                    });
                }
            }
        });

        shopDialog.getWindow().setAttributes(params);
        shopDialog.show();
    }


    @Override
    public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> list) {
        //Satın alma işlemi gerçekleşmiş ise
        if (billingResult.getResponseCode() == Purchase.PurchaseState.PURCHASED)
            handlePurchase(list.get(0));
    }


    private void handlePurchase(Purchase purchase) {
        //Satın alma gerçekleşmiş ise
        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED){
            if (!purchase.isAcknowledged()){
                AcknowledgePurchaseParams purchaseParams = AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.getPurchaseToken())
                        .build();

                AcknowledgePurchaseResponseListener acknowledgePurchaseResponseListener = new AcknowledgePurchaseResponseListener() {
                    @Override
                    public void onAcknowledgePurchaseResponse(@NonNull BillingResult billingResult) {
                        Toast.makeText(getApplicationContext(), "Satın Alma İşlemi Başarıyla Gerçekleşti", Toast.LENGTH_SHORT).show();

                        sonCanDurumu += heartList.get(heartPos);
                        canMiktariniGuncelle(Integer.valueOf(txtUserHeartCount.getText().toString()), sonCanDurumu);
                    }
                };
                mBillingClient.acknowledgePurchase(purchaseParams, acknowledgePurchaseResponseListener);
            }
        }
    }


    //Inner class     Dekerosyon
    private class GridItemDecoration extends RecyclerView.ItemDecoration{
        //İtemler arasına eşit oranda boşluk miktarı koymak için
        private int spanCount;
        private int spacing;


        public GridItemDecoration(int spanCount, int spacing) {
            this.spanCount = spanCount;
            this.spacing = spacing;
        }


        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            //Gelen nesnenin pozisyonunu alıp ona göre ayar yapıyoruz
            int position = parent.getChildAdapterPosition(view);
            int column = position % spanCount;

            //Boşluk bırakma ayarları
            outRect.left = (column + 1) * spacing / spanCount;
            outRect.right = (column + 1) * spacing / spanCount;
            outRect.bottom = spacing;
        }
    }



    public void btnAyarlar(View v) {
        ayarlariAc();
    }


    private void ayarlariAc(){
        settingsDialog = new Dialog(this);
        //Pencere özelliklerini kopyala
        params = new WindowManager.LayoutParams();
        params.copyFrom(settingsDialog.getWindow().getAttributes());
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        //Kapatılma özelliğini pasif hale getir
        settingsDialog.setCancelable(false);
        settingsDialog.setContentView(R.layout.custom_dialog_settings);

        settingsImageClose = (ImageView) settingsDialog.findViewById(R.id.custom_dialog_settings_imageViewClose);
        settingsBtnName = (Button) settingsDialog.findViewById(R.id.custom_dialog_settings_btnChangeName);
        settingsBtnProfile = (Button) settingsDialog.findViewById(R.id.custom_dialog_settings_btnChangeProfileImage);
        settingsRadioOpen = (RadioButton) settingsDialog.findViewById(R.id.custom_dialog_settings_radioBtnMusicOpen);
        settingsRadioClose = (RadioButton) settingsDialog.findViewById(R.id.custom_dialog_settings_radioBtnMusicClose);

        //Kullanıcı açık duruma getirmişse veya default olarak true gelmiş ise settingsRadioOpen işaretlensin, değilse settingsRadioClose
        if (!muzikDurum)
            settingsRadioClose.setChecked(!muzikDurum);
        else
            settingsRadioOpen.setChecked(muzikDurum);

        //Kullanıcı opena basmış ise true olarak kaydet,
        settingsRadioOpen.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b)
                    muzikAcKapatAyar(b);
            }
        });

        //closa basmış ise false olarak kaydet
        settingsRadioClose.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b)
                    muzikAcKapatAyar(!b);
            }
        });


        //ekranı kapat
        settingsImageClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                settingsDialog.dismiss();
            }
        });

        settingsBtnName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                settingsDialog.dismiss();
                isimDegistirDialog();
            }
        });

        settingsBtnProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //İzin verilmediyse
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                    //İzin al
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, izinVer);
                }
                //İzni var ise
                else {
                    //Resmin yolunun belirlenmesi
                    Intent resimDegistirIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                    startActivityForResult(resimDegistirIntent, izinVerildi);
                }
            }
        });

        settingsDialog.getWindow().setAttributes(params);
        settingsDialog.show();
    }


    private void muzikAcKapatAyar(boolean b){
        editor = preferences.edit();
        editor.putBoolean("muzikDurumu", b);
        editor.apply();
    }


    private void canKazanmaMenusu(){
        getHeartDialog = new Dialog(this);
        //Dialog göster
        params = new WindowManager.LayoutParams();
        params.copyFrom(getHeartDialog.getWindow().getAttributes());
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        getHeartDialog.setCancelable(false);
        getHeartDialog.setContentView(R.layout.custom_dialog_get_heart);

        getHeartImgClose = (ImageView)getHeartDialog.findViewById(R.id.custom_dialog_get_heart_imageViewClose);
        getHeartShowAndGet = (ImageView)getHeartDialog.findViewById(R.id.custom_dialog_get_heart_imageViewShowAndGet);
        getHeartBuyAndGet = (ImageView)getHeartDialog.findViewById(R.id.custom_dialog_get_heart_imageViewBuyAndGet);

        getHeartImgClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getHeartDialog.dismiss();
            }
        });

        getHeartShowAndGet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reklamOlustur();
                reklamYukle();
            }
        });

        getHeartBuyAndGet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Shop Menu
                marketDialog();
            }
        });


        //paramsi dialoga dahil ediyoruz
        getHeartDialog.getWindow().setAttributes(params);
        getHeartDialog.show();

    }


    private void isimDegistirDialog(){
        changeNameDialog = new Dialog(this);
        params = new WindowManager.LayoutParams();
        params.copyFrom(changeNameDialog.getWindow().getAttributes());
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;

        //Buton harici biryere dokununca kapanma
        changeNameDialog.setCancelable(false);

        changeNameDialog.setContentView(R.layout.custom_dialog_change_name);

        changeNameImgClose = (ImageView) changeNameDialog.findViewById(R.id.custom_dialog_change_name_close);
        changeNameEditTxtName = (EditText) changeNameDialog.findViewById(R.id.custom_dialog_change_name_editText);
        changeNameDialogbtn = (Button) changeNameDialog.findViewById(R.id.custom_dialog_change_name_btnChangeName);

        changeNameImgClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeNameDialog.dismiss();
            }
        });

        changeNameDialogbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getchangeName = changeNameEditTxtName.getText().toString();
                //Değiştirilecek isim boş değil ise
                if (!TextUtils.isEmpty(getchangeName)){
                    //girilen değer ile önceki isim eşit değil ise
                    if (!(getchangeName.matches(txtViewUserName.getText().toString())))
                        ismiGuncelle(getchangeName, txtViewUserName.getText().toString());
                    else
                        Toast.makeText(getApplicationContext(), "Zaten Bu İsimi Kullanıyorsunuz", Toast.LENGTH_SHORT).show();
                }
                else
                    Toast.makeText(getApplicationContext(), "İsim Değeri Boş Bırakılamaz", Toast.LENGTH_SHORT).show();
            }
        });

        changeNameDialog.getWindow().setAttributes(params);
        changeNameDialog.show();
    }


    //İsmin Güncellenmesi
    private void ismiGuncelle(String yeniDeger, String eskiDeger){
        try {
            sqlSorgusu = "UPDATE Ayarlar SET k_adi = ? WHERE k_adi = ?";
            statement = database.compileStatement(sqlSorgusu);
            statement.bindString(1, yeniDeger);
            statement.bindString(2, eskiDeger);
            statement.execute();

            txtViewUserName.setText(yeniDeger);
            Toast.makeText(getApplicationContext(), "İsminiz Başarıyla Değiştirildi", Toast.LENGTH_SHORT).show();

            if (changeNameDialog.isShowing())
                changeNameDialog.dismiss();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }


    //Resim değiştirme için İzin alınmamış ise
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == izinVer){
            //Kullanıcı izin vermiş ise
            if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                //Resmin yolunun belirlenmesi
                Intent resimDegistirIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                startActivityForResult(resimDegistirIntent, izinVerildi);
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    //Resim değiştirme için İzin alınmış ise
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (requestCode == izinVerildi){
            //result kodu almış ise ve data boş değil ise datayı alacak
            if (resultCode == RESULT_OK && data != null){
                resimUri = data.getData();

                //Ekrana alert dialog açılacak onaylarsa resim yüklenecek ve veritabanına kayıt edilecek
                alertBuilder = new AlertDialog.Builder(this);
                alertBuilder.setTitle("Kelime Bilmece");
                alertBuilder.setMessage("Profil Resminizi Değiştirmek İstediğinize Emin Misiniz?");
                alertBuilder.setIcon(R.mipmap.ic_kelime_bilmecee);
                alertBuilder.setCancelable(false);
                alertBuilder.setPositiveButton("Evet", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        try {
                            if (Build.VERSION.SDK_INT >= 28){
                                //imageDecoder a çevirdik
                                resimDosyasi = ImageDecoder.createSource(MainActivity.this.getContentResolver(), resimUri);
                                //Bitmape çevriyoruz
                                resimBitmap = ImageDecoder.decodeBitmap(resimDosyasi);
                                //resmi CircleImageView içerisine attık
                                userProfileImage.setImageBitmap(resimBitmap);
                            }
                            else {
                                //Gelen resmi dönüştürdük
                                resimBitmap = MediaStore.Images.Media.getBitmap(MainActivity.this.getContentResolver(), resimUri);
                                //Alınan resmi circleImageView içerisine atıyoruz
                                userProfileImage.setImageBitmap(resimBitmap);
                            }

                            profilResminiKaydet(resimBitmap);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
                alertBuilder.setNegativeButton("Hayır", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                alertBuilder.show();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }


    private void profilResminiKaydet(Bitmap profilResmi){

        try {
            //Gelen resmi eziyoruz
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            profilResmi.compress(Bitmap.CompressFormat.PNG, 75, outputStream);
            resimByte = outputStream.toByteArray();


            //Veri tabanına kaydediyoruz
            sqlSorgusu = "UPDATE Ayarlar SET k_image = ? WHERE k_adi = ?";
            statement = database.compileStatement(sqlSorgusu);
            //güncellenecek yeni değer
            statement.bindBlob(1, resimByte);
            //güncelleme şartı     Kullanıcı adı img ile eşit olan kullanıcıyı değiştir
            statement.bindString(2, txtViewUserName.getText().toString());
            statement.execute();

            if (settingsDialog.isShowing())
                settingsDialog.dismiss();

            Toast.makeText(getApplicationContext(), "Profil Resminiz Başarıyla Değiştirildi", Toast.LENGTH_SHORT).show();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

}