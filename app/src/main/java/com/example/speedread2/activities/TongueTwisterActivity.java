package com.example.speedread2.activities;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.speedread2.R;
import com.example.speedread2.utils.BackgroundHelper;
import com.example.speedread2.database.AppDatabase;
import com.example.speedread2.dao.TongueTwisterDao;
import com.example.speedread2.dao.UserDao;
import com.example.speedread2.database.entities.TongueTwister;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Activity для чтения скороговорок
 * Использует распознавание речи для проверки правильности произношения
 */
public class TongueTwisterActivity extends AppCompatActivity {
    
    private static final int PERMISSION_REQUEST_CODE = 100;
    
    private TongueTwisterDao tongueTwisterDao;
    private UserDao userDao;
    private TongueTwister currentTongueTwister;
    private String[] words; // Массив слов скороговорки
    
    private TextView tvCurrentLine;
    private ImageView ivCharacter;
    private Button btnMicrophone;
    private ImageButton btnBack;
    
    private SpeechRecognizer speechRecognizer;
    private Intent speechRecognizerIntent;
    private boolean isListening = false;
    private boolean isReadingStarted = false;
    private boolean isSpeaking = false;
    private boolean isCompleted = false;
    private boolean isRewardGiven = false;
    
    private ObjectAnimator characterAnimator;
    private Handler speechHandler = new Handler(Looper.getMainLooper());
    
    // Цвета для подсветки
    private static final int COLOR_GREEN = Color.parseColor("#4CAF50");
    private static final int COLOR_YELLOW = Color.parseColor("#FFC107");
    private static final int COLOR_RED = Color.parseColor("#F44336");
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reading);
        
        // Применяем фон
        BackgroundHelper.applyBackground(this);
        
        // Инициализация БД
        AppDatabase database = AppDatabase.getInstance(this);
        tongueTwisterDao = database.tongueTwisterDao();
        userDao = database.userDao();
        
        // Получаем ID скороговорки из Intent
        int tongueTwisterId = getIntent().getIntExtra("tongueTwisterId", -1);
        if (tongueTwisterId == -1) {
            Toast.makeText(this, "Ошибка: скороговорка не найдена", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // Загружаем скороговорку из БД
        currentTongueTwister = tongueTwisterDao.getTongueTwisterById(tongueTwisterId);
        if (currentTongueTwister == null || currentTongueTwister.content == null) {
            Toast.makeText(this, "Скороговорка не найдена", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // Разбиваем текст на слова
        words = currentTongueTwister.content.split("\\s+");
        
        // Инициализация UI
        tvCurrentLine = findViewById(R.id.tvCurrentLine);
        ivCharacter = findViewById(R.id.ivCharacter);
        btnMicrophone = findViewById(R.id.btnMicrophone);
        btnBack = findViewById(R.id.btnBack);
        
        // Устанавливаем текст скороговорки
        tvCurrentLine.setText(currentTongueTwister.content);
        resetTextColor();
        
        // Обработчик кнопки назад
        btnBack.setOnClickListener(v -> finish());
        
        // Инициализация SpeechRecognizer
        initializeSpeechRecognizer();
        
        // Обработчик кнопки микрофона
        btnMicrophone.setOnClickListener(v -> {
            if (!isReadingStarted) {
                startReading();
            } else {
                toggleMicrophone();
            }
        });
    }
    
    private void initializeSpeechRecognizer() {
        try {
            if (!SpeechRecognizer.isRecognitionAvailable(this)) {
                Toast.makeText(this, "Распознавание речи недоступно на этом устройстве", Toast.LENGTH_LONG).show();
                btnMicrophone.setEnabled(false);
                return;
            }
            
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
            if (speechRecognizer == null) {
                Toast.makeText(this, "Не удалось создать распознаватель речи", Toast.LENGTH_LONG).show();
                btnMicrophone.setEnabled(false);
                return;
            }
            
            speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ru-RU");
            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
            
            RecognitionListener recognitionListener = new RecognitionListener() {
                @Override
                public void onReadyForSpeech(Bundle params) {
                    Log.d("TongueTwisterActivity", "Готов к распознаванию");
                    runOnUiThread(() -> {
                        tvCurrentLine.setText(currentTongueTwister.content + "\n\n(Говорите...)");
                        resetTextColor();
                    });
                }
                
                @Override
                public void onBeginningOfSpeech() {
                    Log.d("TongueTwisterActivity", "Начало речи");
                    isSpeaking = true;
                    startCharacterAnimation();
                }
                
                @Override
                public void onRmsChanged(float rmsdB) {
                }
                
                @Override
                public void onBufferReceived(byte[] buffer) {
                }
                
                @Override
                public void onEndOfSpeech() {
                    Log.d("TongueTwisterActivity", "Конец речи");
                    isSpeaking = false;
                    stopCharacterAnimation();
                }
                
                @Override
                public void onError(int error) {
                    isSpeaking = false;
                    stopCharacterAnimation();
                    String errorMessage = getErrorText(error);
                    Log.e("TongueTwisterActivity", "Ошибка распознавания: " + errorMessage);
                    
                    if (error == SpeechRecognizer.ERROR_CLIENT) {
                        if (speechRecognizer != null) {
                            try {
                                speechRecognizer.destroy();
                            } catch (Exception e) {
                                Log.e("TongueTwisterActivity", "Ошибка при уничтожении SpeechRecognizer", e);
                            }
                        }
                        initializeSpeechRecognizer();
                        return;
                    }
                    
                    if (error == SpeechRecognizer.ERROR_NO_MATCH || error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT) {
                        highlightAllWordsRed();
                        speechHandler.postDelayed(() -> {
                            if (isReadingStarted && !isCompleted) {
                                startListening();
                            }
                        }, 1000);
                    }
                }
                
                @Override
                public void onResults(Bundle results) {
                    ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    if (matches != null && !matches.isEmpty()) {
                        String recognizedText = matches.get(0);
                        Log.d("TongueTwisterActivity", "Распознано: " + recognizedText);
                        
                        int matchQuality = calculateMatchQuality(recognizedText, currentTongueTwister.content);
                        highlightWordsInLine(recognizedText, true);
                        
                        runOnUiThread(() -> {
                            tvCurrentLine.setText(currentTongueTwister.content);
                            highlightWordsInLine(recognizedText, true);
                        });
                        
                        if (matchQuality >= 70) {
                            handleSuccessfulReading();
                        } else {
                            // Продолжаем слушать
                            speechHandler.postDelayed(() -> {
                                if (isReadingStarted && !isCompleted) {
                                    startListening();
                                }
                            }, 2000);
                        }
                    }
                }
                
                @Override
                public void onPartialResults(Bundle partialResults) {
                    ArrayList<String> matches = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    if (matches != null && !matches.isEmpty()) {
                        String partialText = matches.get(0);
                        highlightWordsInLine(partialText, false);
                    }
                }
                
                @Override
                public void onEvent(int eventType, Bundle params) {
                }
            };
            
            speechRecognizer.setRecognitionListener(recognitionListener);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Ошибка инициализации распознавания речи", Toast.LENGTH_SHORT).show();
            btnMicrophone.setEnabled(false);
        }
    }
    
    private int calculateMatchQuality(String recognized, String expected) {
        if (recognized == null || expected == null) return 0;
        
        recognized = recognized.replaceAll("[^\\p{L}\\p{N}\\s]", "").trim();
        expected = expected.replaceAll("[^\\p{L}\\p{N}\\s]", "").trim();
        
        if (expected.isEmpty()) return 0;
        if (recognized.equals(expected)) return 100;
        
        String[] recognizedWords = recognized.split("\\s+");
        String[] expectedWords = expected.split("\\s+");
        
        int matchedWords = 0;
        for (String expectedWord : expectedWords) {
            for (String recognizedWord : recognizedWords) {
                if (recognizedWord.equals(expectedWord) || 
                    recognizedWord.contains(expectedWord) || 
                    expectedWord.contains(recognizedWord)) {
                    matchedWords++;
                    break;
                }
            }
        }
        
        if (expectedWords.length > 0) {
            return (matchedWords * 100) / expectedWords.length;
        }
        
        return 0;
    }
    
    private void highlightWordsInLine(String recognizedText, boolean isFinalResult) {
        if (tvCurrentLine == null || currentTongueTwister == null) return;
        
        String cleanRecognized = recognizedText.replaceAll("[^\\p{L}\\p{N}\\s]", "").toLowerCase(new Locale("ru", "RU")).trim();
        String[] recognizedWords = cleanRecognized.split("\\s+");
        
        SpannableString spannable = new SpannableString(currentTongueTwister.content);
        boolean[] usedRecognized = new boolean[recognizedWords.length];
        
        int currentPos = 0;
        for (int i = 0; i < words.length; i++) {
            String expectedWord = words[i].replaceAll("[^\\p{L}\\p{N}]", "").toLowerCase(new Locale("ru", "RU"));
            if (expectedWord.isEmpty()) continue;
            
            int wordColor = COLOR_RED;
            boolean wordMatched = false;
            
            for (int j = 0; j < recognizedWords.length; j++) {
                if (usedRecognized[j]) continue;
                
                String recognizedWord = recognizedWords[j];
                if (recognizedWord.isEmpty()) continue;
                
                if (recognizedWord.equals(expectedWord)) {
                    wordMatched = true;
                    wordColor = COLOR_GREEN;
                    usedRecognized[j] = true;
                    break;
                }
                
                if (recognizedWord.contains(expectedWord) || expectedWord.contains(recognizedWord)) {
                    int minLen = Math.min(recognizedWord.length(), expectedWord.length());
                    if (minLen >= 3) {
                        wordMatched = true;
                        wordColor = COLOR_GREEN;
                        usedRecognized[j] = true;
                        break;
                    }
                }
                
                if (expectedWord.length() >= 3 && recognizedWord.length() >= 3) {
                    String expStart = expectedWord.substring(0, Math.min(3, expectedWord.length()));
                    String recStart = recognizedWord.substring(0, Math.min(3, recognizedWord.length()));
                    if (expStart.equals(recStart) && Math.abs(expectedWord.length() - recognizedWord.length()) <= 2) {
                        wordMatched = true;
                        wordColor = COLOR_YELLOW;
                        usedRecognized[j] = true;
                        break;
                    }
                }
            }
            
            if (!wordMatched && !isFinalResult && i < recognizedWords.length && !recognizedWords[i].isEmpty()) {
                wordColor = COLOR_YELLOW;
            }
            
            int wordStart = currentTongueTwister.content.indexOf(words[i], currentPos);
            if (wordStart >= 0) {
                int wordEnd = wordStart + words[i].length();
                spannable.setSpan(new ForegroundColorSpan(wordColor), wordStart, wordEnd, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
                currentPos = wordEnd;
            }
        }
        
        runOnUiThread(() -> tvCurrentLine.setText(spannable));
    }
    
    private void highlightAllWordsRed() {
        if (tvCurrentLine == null || currentTongueTwister == null) return;
        
        SpannableString spannable = new SpannableString(currentTongueTwister.content);
        int currentPos = 0;
        
        for (String word : words) {
            int wordStart = currentTongueTwister.content.indexOf(word, currentPos);
            if (wordStart >= 0) {
                int wordEnd = wordStart + word.length();
                spannable.setSpan(new ForegroundColorSpan(COLOR_RED), wordStart, wordEnd, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
                currentPos = wordEnd;
            }
        }
        
        runOnUiThread(() -> tvCurrentLine.setText(spannable));
    }
    
    private void resetTextColor() {
        if (tvCurrentLine != null && currentTongueTwister != null) {
            tvCurrentLine.setTextColor(Color.parseColor("#000000"));
        }
    }
    
    private void startCharacterAnimation() {
        if (ivCharacter == null || characterAnimator != null) return;
        
        characterAnimator = ObjectAnimator.ofFloat(ivCharacter, "translationX", 0f, 50f, -50f, 0f);
        characterAnimator.setDuration(500);
        characterAnimator.setRepeatCount(ObjectAnimator.INFINITE);
        characterAnimator.setInterpolator(new android.view.animation.LinearInterpolator());
        characterAnimator.start();
    }
    
    private void stopCharacterAnimation() {
        if (characterAnimator != null) {
            characterAnimator.cancel();
            characterAnimator = null;
        }
        if (ivCharacter != null) {
            ivCharacter.setTranslationX(0f);
        }
    }
    
    private void startReading() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSION_REQUEST_CODE);
            return;
        }
        
        isReadingStarted = true;
        btnMicrophone.setText("Остановить");
        resetTextColor();
        startListening();
    }
    
    private void startListening() {
        if (speechRecognizer == null || !isReadingStarted) return;
        
        try {
            isListening = true;
            speechRecognizer.startListening(speechRecognizerIntent);
            Toast.makeText(this, "Говорите...", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e("TongueTwisterActivity", "Ошибка при запуске прослушивания", e);
            Toast.makeText(this, "Ошибка запуска микрофона", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void toggleMicrophone() {
        if (isListening) {
            stopListening();
            btnMicrophone.setText("Продолжить чтение");
        } else {
            startListening();
            btnMicrophone.setText("Остановить");
        }
    }
    
    private void stopListening() {
        if (speechRecognizer != null && isListening) {
            try {
                speechRecognizer.stopListening();
            } catch (Exception e) {
                Log.e("TongueTwisterActivity", "Ошибка при остановке прослушивания", e);
            }
        }
        isListening = false;
        stopCharacterAnimation();
    }
    
    private void handleSuccessfulReading() {
        if (isCompleted) return;

        isCompleted = true;
        isReadingStarted = false;
        stopListening();
        btnMicrophone.setEnabled(false);
        btnMicrophone.setText("Прочитано");

        int rewardCoins = getRewardByDifficulty(currentTongueTwister.difficulty);
        if (!isRewardGiven) {
            SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            int currentUserId = prefs.getInt("currentUserId", -1);
            if (currentUserId != -1) {
                var user = userDao.getUserById(currentUserId);
                if (user != null) {
                    userDao.updateCoins(currentUserId, user.coins + rewardCoins);
                }
            }
            isRewardGiven = true;
        }

        Toast.makeText(this, "Скороговорка успешно прочитана! +" + rewardCoins + " монет", Toast.LENGTH_LONG).show();
    }

    private int getRewardByDifficulty(int difficulty) {
        switch (difficulty) {
            case 1:
                return 3;
            case 2:
                return 6;
            default:
                return 10;
        }
    }

    private String getErrorText(int errorCode) {
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                return "Ошибка аудио";
            case SpeechRecognizer.ERROR_CLIENT:
                return "Ошибка клиента";
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                return "Недостаточно прав";
            case SpeechRecognizer.ERROR_NETWORK:
                return "Ошибка сети";
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                return "Таймаут сети";
            case SpeechRecognizer.ERROR_NO_MATCH:
                return "Нет совпадений";
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                return "Распознаватель занят";
            case SpeechRecognizer.ERROR_SERVER:
                return "Ошибка сервера";
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                return "Таймаут речи";
            default:
                return "Неизвестная ошибка";
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startReading();
            } else {
                Toast.makeText(this, "Разрешение на микрофон необходимо для работы приложения", Toast.LENGTH_LONG).show();
            }
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
        if (speechHandler != null) {
            speechHandler.removeCallbacksAndMessages(null);
        }
        stopCharacterAnimation();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        stopListening();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        if (isReadingStarted && !isListening && !isCompleted) {
            startListening();
        }
    }
    
    /**
     * Применяет выбранный фон из настроек (по умолчанию белый)
     */
}

