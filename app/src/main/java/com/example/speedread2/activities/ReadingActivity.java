package com.example.speedread2.activities;

import android.Manifest;
import android.animation.AnimatorSet;
import android.animation.Keyframe;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
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
import com.example.speedread2.dao.QuestionDao;
import com.example.speedread2.dao.TextDao;
import com.example.speedread2.database.entities.Question;
import com.example.speedread2.database.entities.Text;
import com.example.speedread2.speech.VoskManager;
import com.example.speedread2.speech.VoskModelLoader;
import com.example.speedread2.speech.VoskModelState;

import org.vosk.Model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class ReadingActivity extends AppCompatActivity {
    
    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final int SPEECH_REQUEST_CODE = 200;
    
    private TextDao textDao;
    private QuestionDao questionDao;
    private Text currentText;
    private String[] lines;
    private String[][] lineWords; // Массив массивов слов для каждой строки
    private int currentLineIndex = 0;
    private int currentWordIndex = 0; // Текущее слово в строке
    
    private TextView tvCurrentLine;
    private ImageView ivCharacter;
    private Button btnMicrophone;
    private ImageButton btnBack;
    
    private VoskManager voskManager;
    private Model voskModel;
    private boolean isModelReady = false;
    private boolean isModelLoading = false;
    private boolean isListening = false;
    private boolean isReadingStarted = false;
    private boolean isSpeaking = false; // Флаг активного чтения
    
    private AnimatorSet characterAnimator;
    private View characterTrack;
    private Handler speechHandler = new Handler(Looper.getMainLooper());
    private Runnable speechTimeoutRunnable;
    
    // Статистика чтения
    private long readingStartTime = 0;
    private int totalWordsRead = 0;
    private int correctWords = 0;
    private int unclearWords = 0;
    private int incorrectWords = 0;
    
    // Цвета для подсветки
    private static final int COLOR_GREEN = Color.parseColor("#4CAF50"); // Четко
    private static final int COLOR_YELLOW = Color.parseColor("#FFC107"); // Непонятно, но зачтено
    private static final int COLOR_RED = Color.parseColor("#F44336"); // Нечетко/ничего
    private static final int COLOR_DEFAULT = Color.parseColor("#FFFFFF"); // Белый (по умолчанию)
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reading);
        
        // Применяем фон
        BackgroundHelper.applyBackground(this);
        
        // Инициализация БД
        AppDatabase database = AppDatabase.getInstance(this);
        textDao = database.textDao();
        questionDao = database.questionDao();
        
        // Получаем ID текста из Intent
        int textId = getIntent().getIntExtra("textId", -1);
        if (textId == -1) {
            Toast.makeText(this, "Ошибка: текст не найден", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // Загружаем текст из БД
        currentText = textDao.getTextById(textId);
        if (currentText == null) {
            Toast.makeText(this, "Текст не найден", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // Разбиваем текст на строки - используем content, если lines пусто
        String textToSplit = (currentText.content != null && !currentText.content.isEmpty()) 
            ? currentText.content 
            : (currentText.lines != null ? currentText.lines : "");
        
        if (textToSplit.isEmpty()) {
            Toast.makeText(this, "Текст пуст", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        lines = splitTextIntoLines(textToSplit);
        lineWords = new String[lines.length][];
        for (int i = 0; i < lines.length; i++) {
            lineWords[i] = lines[i].split("\\s+");
        }
        
        // Инициализация UI
        tvCurrentLine = findViewById(R.id.tvCurrentLine);
        characterTrack = findViewById(R.id.characterTrack);
        ivCharacter = findViewById(R.id.ivCharacter);
        btnMicrophone = findViewById(R.id.btnMicrophone);
        btnBack = findViewById(R.id.btnBack);
        
        // Устанавливаем первую строку
        if (lines.length > 0) {
            tvCurrentLine.setText(lines[0]);
        }
        
        // Обработчик кнопки назад
        btnBack.setOnClickListener(v -> finish());
        
        // Инициализация Vosk (до обработчиков)
        initializeVosk();
        
        // Обработчик кнопки микрофона
        btnMicrophone.setOnClickListener(v -> {
            if (!isReadingStarted) {
                startReading();
            } else {
                toggleMicrophone();
            }
        });
    }
    
    /**
     * Разбивает текст на строки по предложениям и фразам
     */
    private String[] splitTextIntoLines(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new String[0];
        }
        
        // Сначала разбиваем по переносам строк
        String[] paragraphs = text.split("\n");
        ArrayList<String> lines = new ArrayList<>();
        
        for (String paragraph : paragraphs) {
            if (paragraph.trim().isEmpty()) continue;
            
            // Разбиваем по знакам конца предложения
            String[] sentences = paragraph.split("(?<=[.!?])\\s+");
            
            for (String sentence : sentences) {
                sentence = sentence.trim();
                if (sentence.isEmpty()) continue;
                
                // Если предложение длинное, разбиваем по запятым
                if (sentence.length() > 80) {
                    String[] phrases = sentence.split("(?<=[,;])\\s+");
                    for (String phrase : phrases) {
                        phrase = phrase.trim();
                        if (!phrase.isEmpty()) {
                            lines.add(phrase);
                        }
                    }
                } else {
                    lines.add(sentence);
                }
            }
        }
        
        return lines.toArray(new String[0]);
    }
    
    private void initializeVosk() {
        if (isModelLoading) {
            return;
        }

        String grammarSource = currentText != null && currentText.content != null
            ? currentText.content
            : String.join(" ", lines);

        VoskModelLoader.load(this, "model-ru", state -> {
            if (state instanceof VoskModelState.Loading) {
                isModelLoading = true;
                isModelReady = false;
                runOnUiThread(() -> {
                    btnMicrophone.setEnabled(false);
                    btnMicrophone.setText("Загрузка модели...");
                });
            }

            if (state instanceof VoskModelState.Success) {
                isModelLoading = false;
                if (voskModel != null) {
                    try {
                        voskModel.close();
                    } catch (Exception ignored) {
                    }
                }
                voskModel = ((VoskModelState.Success) state).getModel();
                if (voskManager != null) {
                    voskManager.close();
                }
                voskManager = new VoskManager(voskModel, grammarSource, 16000, null);
                isModelReady = true;
                runOnUiThread(() -> {
                    btnMicrophone.setEnabled(true);
                    btnMicrophone.setText(isReadingStarted ? "Остановить" : "Начать");
                });
            }

            if (state instanceof VoskModelState.Error) {
                isModelLoading = false;
                isModelReady = false;
                Throwable error = ((VoskModelState.Error) state).getThrowable();
                Log.e("ReadingActivity", "Ошибка загрузки Vosk модели", error);
                runOnUiThread(() -> {
                    btnMicrophone.setEnabled(false);
                    Toast.makeText(this, "Не удалось загрузить модель распознавания", Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private final VoskManager.Callback voskCallback = new VoskManager.Callback() {
        @Override
        public void onPartialText(String text) {
            runOnUiThread(() -> {
                if (currentLineIndex < lines.length) {
                    highlightWordsInLine(text, false);
                }
            });
        }

        @Override
        public void onFinalText(String text) {
            runOnUiThread(() -> handleFinalRecognition(text));
        }

        @Override
        public void onError(String error) {
            Log.e("ReadingActivity", "Vosk error: " + error);
            runOnUiThread(() -> {
                if (isListening && currentLineIndex < lines.length) {
                    highlightAllWordsRed();
                    scheduleRestartListening(900);
                }
            });
        }
    };

    private void handleFinalRecognition(String recognizedText) {
        if (currentLineIndex >= lines.length) {
            return;
        }

        String expectedText = lines[currentLineIndex].trim();
        String normalized = recognizedText == null ? "" : recognizedText.trim();

        Log.d("ReadingActivity", "=== VOSK RESULT ===");
        Log.d("ReadingActivity", "Распознано: '" + normalized + "'");
        Log.d("ReadingActivity", "Ожидается: '" + expectedText + "'");

        int matchQuality = highlightWordsInLine(normalized, true);
        updateReadingStats(matchQuality);

        if (matchQuality >= 40) {
            speechHandler.postDelayed(() -> {
                if (isListening) {
                    moveToNextLine();
                }
            }, matchQuality >= 60 ? 220 : 380);
        } else {
            highlightAllWordsRed();
            scheduleRestartListening(1100);
        }
    }
    
    /**
     * Вычисляет качество совпадения распознанного текста с ожидаемым (0-100)
     */
    private int calculateMatchQuality(String recognized, String expected) {
        if (recognized == null || expected == null) return 0;
        
        // Убираем знаки препинания для сравнения
        recognized = recognized.replaceAll("[^\\p{L}\\p{N}\\s]", "").trim();
        expected = expected.replaceAll("[^\\p{L}\\p{N}\\s]", "").trim();

        if (expected.isEmpty() || recognized.isEmpty()) return 0;
        
        // Проверяем точное совпадение
        if (recognized.equals(expected)) {
            return 100;
        }
        
        // Проверяем, содержит ли распознанный текст ожидаемый
        if (recognized.contains(expected) || expected.contains(recognized)) {
            return 85;
        }
        
        // Вычисляем процент совпадения слов
        String[] recognizedWords = recognized.split("\\s+");
        String[] expectedWords = expected.split("\\s+");
        
        int matchedWords = 0;
        for (String expectedWord : expectedWords) {
            if (expectedWord.isEmpty()) {
                continue;
            }

            for (String recognizedWord : recognizedWords) {
                if (recognizedWord.isEmpty()) {
                    continue;
                }

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
    
    /**
     * Подсвечивает слова в текущей строке на основе распознанного текста
     * @param recognizedText распознанный текст
     * @param isFinalResult true если это финальный результат, false если частичный
     * @return процент совпадения (0-100)
     */
    private int highlightWordsInLine(String recognizedText, boolean isFinalResult) {
        if (tvCurrentLine == null || currentLineIndex >= lines.length) {
            return 0;
        }
        
        // Нормализуем тексты для сравнения (убираем знаки препинания, приводим к нижнему регистру)
        String cleanRecognized = recognizedText.replaceAll("[^\\p{L}\\p{N}\\s]", "").toLowerCase(new Locale("ru", "RU")).trim();
        String[] recognizedWords = cleanRecognized.split("\\s+");
        String[] expectedWords = lineWords[currentLineIndex];
        
        Log.d("ReadingActivity", "Ожидаемые слова: " + java.util.Arrays.toString(expectedWords));
        Log.d("ReadingActivity", "Распознанные слова: " + java.util.Arrays.toString(recognizedWords));
        
        SpannableString spannable = new SpannableString(lines[currentLineIndex]);
        int matchedWords = 0;
        int currentPos = 0;
        boolean[] usedRecognized = new boolean[recognizedWords.length];
        
        for (int i = 0; i < expectedWords.length; i++) {
            String expectedWord = expectedWords[i].replaceAll("[^\\p{L}\\p{N}]", "").toLowerCase(new Locale("ru", "RU"));
            if (expectedWord.isEmpty()) continue;
            
            boolean wordMatched = false;
            int wordColor = COLOR_RED;
            
            // Проверяем, есть ли это слово в распознанном тексте (более гибкое сравнение)
            for (int j = 0; j < recognizedWords.length; j++) {
                if (usedRecognized[j]) continue;
                
                String recognizedWord = recognizedWords[j];
                if (recognizedWord.isEmpty()) continue;
                
                // Точное совпадение
                if (recognizedWord.equals(expectedWord)) {
                    wordMatched = true;
                    wordColor = COLOR_GREEN;
                    matchedWords++;
                    usedRecognized[j] = true;
                    if (isFinalResult) correctWords++;
                    break;
                }
                
                // Одно слово содержит другое (для склонений и опечаток)
                if (recognizedWord.contains(expectedWord) || expectedWord.contains(recognizedWord)) {
                    int minLen = Math.min(recognizedWord.length(), expectedWord.length());
                    if (minLen >= 3) { // Минимум 3 символа для совпадения
                        wordMatched = true;
                        wordColor = COLOR_GREEN;
                        matchedWords++;
                        usedRecognized[j] = true;
                        if (isFinalResult) correctWords++;
                        break;
                    }
                }
                
                // Проверяем похожесть по началу слова (для опечаток)
                if (expectedWord.length() >= 3 && recognizedWord.length() >= 3) {
                    String expStart = expectedWord.substring(0, Math.min(3, expectedWord.length()));
                    String recStart = recognizedWord.substring(0, Math.min(3, recognizedWord.length()));
                    if (expStart.equals(recStart) && Math.abs(expectedWord.length() - recognizedWord.length()) <= 2) {
                        wordMatched = true;
                        wordColor = COLOR_YELLOW; // Желтый для похожих слов
                        matchedWords++;
                        usedRecognized[j] = true;
                        if (isFinalResult) unclearWords++;
                        break;
                    }
                }
            }
            
            // Если слово не найдено, но это частичный результат - желтый
            if (!wordMatched && !isFinalResult && i < recognizedWords.length && !recognizedWords[i].isEmpty()) {
                wordColor = COLOR_YELLOW;
            } else if (!wordMatched && isFinalResult) {
                incorrectWords++;
            }
            
            // Находим позицию слова в исходном тексте
            int wordStart = lines[currentLineIndex].indexOf(expectedWords[i], currentPos);
            if (wordStart >= 0) {
                int wordEnd = wordStart + expectedWords[i].length();
                spannable.setSpan(new ForegroundColorSpan(wordColor), wordStart, wordEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                currentPos = wordEnd;
            }
        }
        
        runOnUiThread(() -> {
            tvCurrentLine.setText(spannable);
        });
        
        if (expectedWords.length > 0) {
            int quality = (matchedWords * 100) / expectedWords.length;
            Log.d("ReadingActivity", "Совпало слов: " + matchedWords + " из " + expectedWords.length + " = " + quality + "%");
            return quality;
        }
        return 0;
    }
    
    /**
     * Подсвечивает все слова красным
     */
    private void highlightAllWordsRed() {
        if (tvCurrentLine == null || currentLineIndex >= lines.length) {
            return;
        }
        
        SpannableString spannable = new SpannableString(lines[currentLineIndex]);
        String[] words = lineWords[currentLineIndex];
        int currentPos = 0;
        
        for (String word : words) {
            int wordStart = lines[currentLineIndex].indexOf(word, currentPos);
            if (wordStart >= 0) {
                int wordEnd = wordStart + word.length();
                spannable.setSpan(new ForegroundColorSpan(COLOR_RED), wordStart, wordEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                currentPos = wordEnd;
            }
        }
        
        tvCurrentLine.setText(spannable);
    }
    
    /**
     * Сбрасывает цвет текста на черный (только для обычного текста, не для SpannableString)
     */
    private void resetTextColor() {
        if (tvCurrentLine != null) {
            // Не используем setTextColor для SpannableString - он перезапишет цвета
            // Просто устанавливаем обычный текст, если нужно
            tvCurrentLine.setBackgroundColor(COLOR_DEFAULT);
        }
    }
    
    /**
     * Устанавливает обычный черный текст (без подсветки)
     */
    private void setPlainText(String text) {
        if (tvCurrentLine != null) {
            tvCurrentLine.setText(text);
            tvCurrentLine.setTextColor(Color.BLACK);
        }
    }
    
    /**
     * Обновляет статистику чтения
     */
    private void updateReadingStats(int matchQuality) {
        if (currentLineIndex < lineWords.length) {
            totalWordsRead += lineWords[currentLineIndex].length;
        }
        
        if (matchQuality >= 70) {
            // Хорошее совпадение
        } else if (matchQuality >= 40) {
            unclearWords++;
        } else {
            incorrectWords++;
        }
    }
    
    /**
     * Показывает результаты чтения и переходит к вопросам
     */
    private void showReadingResults() {
        // Вычисляем статистику
        long readingTime = (System.currentTimeMillis() - readingStartTime) / 1000; // в секундах
        int readingSpeed = 0;
        if (readingTime > 0) {
            readingSpeed = (int) ((totalWordsRead * 60) / readingTime); // слов в минуту
        }
        
        int clarity = 0;
        if (totalWordsRead > 0) {
            clarity = (correctWords * 100) / totalWordsRead;
        }
        
        // Загружаем вопросы из БД
        List<Question> questions = questionDao.getQuestionsByTextId(currentText.id);
        
        // Переходим к экрану результатов
        Intent intent = new Intent(this, ReadingResultsActivity.class);
        intent.putExtra("textId", currentText.id);
        intent.putExtra("readingSpeed", readingSpeed);
        intent.putExtra("clarity", clarity);
        intent.putExtra("totalWords", totalWordsRead);
        intent.putExtra("correctWords", correctWords);
        intent.putExtra("unclearWords", unclearWords);
        intent.putExtra("incorrectWords", incorrectWords);
        startActivity(intent);
        finish();
    }
    
    /**
     * Отменяет таймаут речи
     */
    private void cancelSpeechTimeout() {
        if (speechTimeoutRunnable != null) {
            speechHandler.removeCallbacks(speechTimeoutRunnable);
        }
    }
    
    private void startReading() {
        // Проверяем разрешение на микрофон
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            Log.d("ReadingActivity", "Запрашиваем разрешение на микрофон");
            ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.RECORD_AUDIO},
                PERMISSION_REQUEST_CODE);
            return;
        }

        if (!isModelReady || voskManager == null) {
            Toast.makeText(this, "Модель распознавания еще загружается", Toast.LENGTH_SHORT).show();
            initializeVosk();
            return;
        }

        Log.d("ReadingActivity", "Начинаем чтение");
        isReadingStarted = true;
        btnMicrophone.setText("Остановить");
        if (tvCurrentLine != null && currentLineIndex < lines.length) {
            setPlainText(lines[currentLineIndex]);
        }

        startListening();
    }

    private void toggleMicrophone() {
        if (isListening) {
            stopListening();
            stopCharacterAnimation();
            resetTextColor();
            btnMicrophone.setText("Продолжить");
        } else {
            resetTextColor();
            startListening();
            btnMicrophone.setText("Остановить");
        }
    }

    private void startListening() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            Log.e("ReadingActivity", "Нет разрешения на микрофон");
            Toast.makeText(this, "Нет разрешения на микрофон", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isReadingStarted) {
            return;
        }

        if (!isModelReady || voskManager == null) {
            Toast.makeText(this, "Модель распознавания еще загружается", Toast.LENGTH_SHORT).show();
            initializeVosk();
            return;
        }

        isListening = true;
        voskManager.start(voskCallback);
        Log.d("ReadingActivity", "Vosk распознавание запущено");
    }

    private void scheduleRestartListening(long delayMs) {
        speechHandler.postDelayed(() -> {
            if (isListening && isReadingStarted && !isSpeaking && voskManager != null) {
                voskManager.stop();
                startListening();
            }
        }, delayMs);
    }

    private void stopListening() {
        if (isListening) {
            isListening = false;
            if (voskManager != null) {
                voskManager.stop();
            }
        }
    }

    private void moveToNextLine() {
        if (currentLineIndex < lines.length - 1) {
            currentLineIndex++;
            currentWordIndex = 0;
            setPlainText(lines[currentLineIndex]); // Устанавливаем обычный текст для новой строки
            
            // Продолжаем слушать для следующей строки
            if (isListening && voskManager != null) {
                scheduleRestartListening(180);
            }
        } else {
            // Текст закончен - показываем результаты
            stopListening();
            stopCharacterAnimation();
            showReadingResults();
        }
    }
    
    private void startCharacterAnimation() {
        if (ivCharacter == null || characterTrack == null || characterAnimator != null) {
            return;
        }

        ivCharacter.setVisibility(View.VISIBLE);
        characterTrack.post(() -> {
            if (characterAnimator != null) {
                return;
            }

            float maxX = Math.max(120f, characterTrack.getWidth() - ivCharacter.getWidth() - 24f);
            float laneCenter = Math.max(0f, (characterTrack.getHeight() - ivCharacter.getHeight()) / 2f);
            float laneTop = Math.max(0f, laneCenter - 55f);
            ObjectAnimator moveX = ObjectAnimator.ofFloat(ivCharacter, "translationX", 0f, maxX);
            moveX.setDuration(4200);
            moveX.setRepeatCount(ValueAnimator.INFINITE);
            moveX.setRepeatMode(ValueAnimator.RESTART);
            moveX.setInterpolator(new LinearInterpolator());

            PropertyValuesHolder pvhY = PropertyValuesHolder.ofKeyframe(
                "translationY",
                Keyframe.ofFloat(0f, laneCenter),
                Keyframe.ofFloat(0.18f, laneTop),
                Keyframe.ofFloat(0.36f, laneTop),
                Keyframe.ofFloat(0.52f, laneCenter),
                Keyframe.ofFloat(0.70f, laneTop),
                Keyframe.ofFloat(0.88f, laneCenter),
                Keyframe.ofFloat(1f, laneCenter)
            );
            ObjectAnimator moveY = ObjectAnimator.ofPropertyValuesHolder(ivCharacter, pvhY);
            moveY.setDuration(4200);
            moveY.setRepeatCount(ValueAnimator.INFINITE);
            moveY.setRepeatMode(ValueAnimator.RESTART);
            moveY.setInterpolator(new LinearInterpolator());

            characterAnimator = new AnimatorSet();
            characterAnimator.playTogether(moveX, moveY);
            characterAnimator.start();
        });
    }
    
    private void stopCharacterAnimation() {
        if (characterAnimator != null) {
            characterAnimator.cancel();
            characterAnimator = null;
        }
        if (ivCharacter != null) {
            ivCharacter.setTranslationX(0f);
            ivCharacter.setTranslationY(0f);
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Разрешение получено - запускаем чтение
                Log.d("ReadingActivity", "Разрешение получено, запускаем чтение");
                startReading();
            } else {
                Toast.makeText(this, "Разрешение на микрофон необходимо для работы приложения", Toast.LENGTH_LONG).show();
            }
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelSpeechTimeout();
        stopCharacterAnimation();
        if (voskManager != null) {
            voskManager.close();
            voskManager = null;
        }
        if (voskModel != null) {
            try {
                voskModel.close();
            } catch (Exception ignored) {
            }
            voskModel = null;
        }
        if (speechHandler != null) {
            speechHandler.removeCallbacksAndMessages(null);
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        if (isListening && voskManager != null) {
            voskManager.stop();
            isListening = false;
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        if (isReadingStarted && !isListening) {
            startListening();
        }
    }
    
    /**
     * Применяет выбранный фон из настроек (по умолчанию белый)
     */
}
