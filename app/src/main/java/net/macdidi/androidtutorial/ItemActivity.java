package net.macdidi.androidtutorial;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TimePicker;

import java.io.File;
import java.util.Calendar;
import java.util.Date;


public class ItemActivity extends ActionBarActivity {

    private EditText title_text, content_text;
    // 啟動功能用的請求代碼
    private static final int START_CAMERA = 0;
    private static final int START_RECORD = 1;
    private static final int START_LOCATION = 2;
    private static final int START_ALARM = 3;
    private static final int START_COLOR = 4;

    // 記事物件
    private Item item;

    // 檔案名稱
    private String fileName;
    // 照片
    private ImageView picture;

    // 錄音檔案名稱
    private String recFileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item);

        processViews();

        // 取得Intent物件
        Intent intent = getIntent();
        // 讀取Action名稱
        String action = intent.getAction();

        // 如果是修改記事
        if (action.equals("net.macdidi.androidtutorial.EDIT_ITEM")) {
            // 接收與設定記事標題
            item = (Item) intent.getExtras().getSerializable(
                    "net.macdidi.androidtutorial.Item");
            title_text.setText(item.getTitle());
            content_text.setText(item.getContent());
        }else {
            item = new Item();
        }
    }

    private void processViews() {
        title_text = (EditText) findViewById(R.id.title_text);
        content_text = (EditText) findViewById(R.id.content_text);
        // 取得顯示照片的ImageView元件
        picture = (ImageView) findViewById(R.id.picture);
    }

    public void onSubmit(View view) {
        // 確定按鈕
        if (view.getId() == R.id.ok_teim) {
            // 讀取使用者輸入的標題與內容
            String titleText = title_text.getText().toString();
            String contentText = content_text.getText().toString();

            // 設定記事物件的標題與內容
            item.setTitle(titleText);
            item.setContent(contentText);

            // 如果是修改記事
            if (getIntent().getAction().equals(
                    "net.macdidi.androidtutorial.EDIT_ITEM")) {
                item.setLastModify(new Date().getTime());
            }
            // 新增記事
            else {
                item.setDatetime(new Date().getTime());
                // 建立SharedPreferences物件
                SharedPreferences sharedPreferences =
                        PreferenceManager.getDefaultSharedPreferences(this);
                // 讀取設定的預設顏色
                int color = sharedPreferences.getInt("DEFAULT_COLOR", -1);
                item.setColor(getColors(color));
            }

            Intent result = getIntent();
            // 設定回傳的記事物件
            result.putExtra("net.macdidi.androidtutorial.Item", item);
            setResult(Activity.RESULT_OK, result);
        }

        // 結束
        finish();
    }

    // 以後需要擴充的功能
    public void clickFunction(View view) {
        int id = view.getId();

        switch (id) {
            case R.id.take_picture:
                // 啟動相機元件用的Intent物件
                Intent intentCamera =
                        new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                // 照片檔案名稱
                File pictureFile = configFileName("P", ".jpg");
                Uri uri = Uri.fromFile(pictureFile);
                // 設定檔案名稱
                intentCamera.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                // 啟動相機元件
                startActivityForResult(intentCamera, START_CAMERA);
                break;
            case R.id.record_sound:
                // 錄音檔案名稱
                final File recordFile = configRecFileName("R", ".mp3");

                // 如果已經有錄音檔，詢問播放或重新錄製
                if (recordFile.exists()) {
                    // 詢問播放還是重新錄製的對話框
                    AlertDialog.Builder d = new AlertDialog.Builder(this);

                    d.setTitle(R.string.title_record)
                            .setCancelable(false);
                    d.setPositiveButton(R.string.record_play,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // 播放
                                    Intent playIntent = new Intent(
                                            ItemActivity.this, PlayActivity.class);
                                    playIntent.putExtra("fileName",
                                            recordFile.getAbsolutePath());
                                    startActivity(playIntent);
                                }
                            });
                    d.setNeutralButton(R.string.record_new,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    goToRecord(recordFile);
                                }
                            });
                    d.setNegativeButton(android.R.string.cancel, null);

                    // 顯示對話框
                    d.show();
                }
                // 如果沒有錄音檔，啟動錄音元件
                else {
                    goToRecord(recordFile);
                }
                break;
            case R.id.set_location:
                // 啟動地圖元件用的Intent物件
                Intent intentMap = new Intent(this, MapsActivity.class);

                // 設定儲存的座標
                intentMap.putExtra("lat", item.getLatitude());
                intentMap.putExtra("lng", item.getLongitude());
                intentMap.putExtra("title", item.getTitle());
                intentMap.putExtra("datetime", item.getLocaleDatetime());

                // 啟動地圖元件
                startActivityForResult(intentMap, START_LOCATION);
                break;
            case R.id.set_alarm:
                // 設定提醒日期時間
                processSetAlarm();
                break;
            case R.id.select_color:
                // 啟動設定顏色的Activity元件
                startActivityForResult(
                        new Intent(this, ColorActivity.class), START_COLOR);
                break;
        }
    }

    private void goToRecord(File recordFile) {
        // 錄音
        Intent recordIntent = new Intent(this, RecordActivity.class);
        recordIntent.putExtra("fileName", recordFile.getAbsolutePath());
        startActivityForResult(recordIntent, START_RECORD);
    }

    private File configFileName(String prefix, String extension) {
        // 如果記事資料已經有檔案名稱
        if (item.getFileName() != null && item.getFileName().length() > 0) {
            fileName = item.getFileName();
        }
        // 產生檔案名稱
        else {
            fileName = FileUtil.getUniqueFileName();
        }

        return new File(FileUtil.getExternalStorageDir(FileUtil.APP_DIR),
                prefix + fileName + extension);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_item, menu);
        return true;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case START_CAMERA:
                    // 設定照片檔案名稱
                    item.setFileName(fileName);
                    break;
                case START_RECORD:
                    // 設定錄音檔案名稱
                    item.setRecFileName(recFileName);
                    break;
                case START_LOCATION:
                    // 讀取與設定座標
                    double lat = data.getDoubleExtra("lat", 0.0);
                    double lng = data.getDoubleExtra("lng", 0.0);
                    item.setLatitude(lat);
                    item.setLongitude(lng);
                    break;
                case START_ALARM:
                    break;
                // 設定顏色
                case START_COLOR:
                    int colorId = data.getIntExtra(
                            "colorId", Colors.LIGHTGREY.parseColor());
                    item.setColor(getColors(colorId));
                    break;
            }
        }
    }

    public static  Colors getColors(int color) {
        Colors result = Colors.LIGHTGREY;

        if (color == Colors.BLUE.parseColor()) {
            result = Colors.BLUE;
        }
        else if (color == Colors.PURPLE.parseColor()) {
            result = Colors.PURPLE;
        }
        else if (color == Colors.GREEN.parseColor()) {
            result = Colors.GREEN;
        }
        else if (color == Colors.ORANGE.parseColor()) {
            result = Colors.ORANGE;
        }
        else if (color == Colors.RED.parseColor()) {
            result = Colors.RED;
        }

        return result;
    }

    @Override
    protected void onResume() {
        super.onResume();

        // 如果有檔案名稱
        if (item.getFileName() != null && item.getFileName().length() > 0) {
            // 照片檔案物件
            File file = configFileName("P", ".jpg");

            // 如果照片檔案存在
            if (file.exists()) {
                // 顯示照片元件
                picture.setVisibility(View.VISIBLE);
                // 設定照片
                FileUtil.fileToImageView(file.getAbsolutePath(), picture);
            }
        }
    }

    private File configRecFileName(String prefix, String extension) {
        // 如果記事資料已經有檔案名稱
        if (item.getRecFileName() != null && item.getRecFileName().length() > 0) {
            recFileName = item.getRecFileName();
        }
        // 產生檔案名稱
        else {
            recFileName = FileUtil.getUniqueFileName();
        }

        return new File(FileUtil.getExternalStorageDir(FileUtil.APP_DIR),
                prefix + recFileName + extension);
    }

    // 設定提醒日期時間
    private void processSetAlarm() {
        Calendar calendar = Calendar.getInstance();

        if (item.getAlarmDatetime() != 0) {
            // 設定為已經儲存的提醒日期時間
            calendar.setTimeInMillis(item.getAlarmDatetime());
        }

        // 讀取年、月、日、時、分
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        // 儲存設定的提醒日期時間
        final Calendar alarm = Calendar.getInstance();

        // 設定提醒時間
        TimePickerDialog.OnTimeSetListener timeSetListener =
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view,
                                          int hourOfDay, int minute) {
                        alarm.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        alarm.set(Calendar.MINUTE, minute);

                        item.setAlarmDatetime(alarm.getTimeInMillis());
                    }
                };

        // 選擇時間對話框
        final TimePickerDialog tpd = new TimePickerDialog(
                this, timeSetListener, hour, minute, true);

        // 設定提醒日期
        DatePickerDialog.OnDateSetListener dateSetListener =
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view,
                                          int year,
                                          int monthOfYear,
                                          int dayOfMonth) {
                        alarm.set(Calendar.YEAR, year);
                        alarm.set(Calendar.MONTH, monthOfYear);
                        alarm.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        // 繼續選擇提醒時間
                        tpd.show();
                    }
                };

        // 建立與顯示選擇日期對話框
        final DatePickerDialog dpd = new DatePickerDialog(
                this, dateSetListener, year, month, day);
        dpd.show();
    }


}
