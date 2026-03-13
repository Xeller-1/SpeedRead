package com.example.speedread2.database;

import android.content.Context;

import com.example.speedread2.dao.CategoryDao;
import com.example.speedread2.dao.QuestionDao;
import com.example.speedread2.dao.ShopItemDao;
import com.example.speedread2.dao.TextDao;
import com.example.speedread2.dao.TongueTwisterDao;
import com.example.speedread2.database.entities.Category;
import com.example.speedread2.database.entities.Question;
import com.example.speedread2.database.entities.ShopItem;
import com.example.speedread2.database.entities.Text;
import com.example.speedread2.database.entities.TongueTwister;

import java.util.ArrayList;
import java.util.List;

public class DatabaseInitializer {
    
    public static void initializeDatabase(Context context) {
        AppDatabase database = AppDatabase.getInstance(context);
        
        // Инициализируем категории
        initializeCategories(database.categoryDao());
        
        // Инициализируем тексты
        List<Text> texts = initializeTexts(database.textDao(), database.categoryDao());
        
        // Инициализируем вопросы для текстов
        initializeQuestions(database.questionDao(), texts);
        
        // Инициализируем скороговорки
        initializeTongueTwisters(database.tongueTwisterDao());
        
        // Инициализируем товары магазина
        initializeShopItems(database.shopItemDao());
    }
    
    private static void initializeCategories(CategoryDao categoryDao) {
        try {
            // Проверяем, есть ли уже категории
            if (categoryDao.getAllCategories().isEmpty()) {
                List<Category> categories = new ArrayList<>();
                
                categories.add(new Category("Стихи", "Коллекция стихотворений для тренировки чтения", 
                    android.R.drawable.ic_menu_edit));
                categories.add(new Category("Рассказы", "Короткие рассказы для развития скорости чтения", 
                    android.R.drawable.ic_menu_view));
                categories.add(new Category("Басни", "Басни для отработки выразительности чтения", 
                    android.R.drawable.ic_menu_agenda));
                
                categoryDao.insertCategories(categories);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static List<Text> initializeTexts(TextDao textDao, CategoryDao categoryDao) {
        List<Text> texts = new ArrayList<>();
        try {
            // Проверяем, есть ли уже тексты
            List<Text> existingTexts = textDao.getAllTexts();
            if (!existingTexts.isEmpty()) {
                return existingTexts; // Возвращаем существующие тексты
            }
            
            List<Category> categories = categoryDao.getAllCategories();
            int poemsCategoryId = 0, storiesCategoryId = 0, fablesCategoryId = 0;
            
            for (Category cat : categories) {
                if ("Стихи".equals(cat.name)) poemsCategoryId = cat.id;
                else if ("Рассказы".equals(cat.name)) storiesCategoryId = cat.id;
                else if ("Басни".equals(cat.name)) fablesCategoryId = cat.id;
            }
            
            // Стихи
            if (poemsCategoryId > 0) {
                texts.add(new Text(poemsCategoryId, 
                    "Белая берёза",
                    "Под моим окном\nПринакрылась снегом,\nТочно серебром.\n\nНа пушистых ветках\nСнежною каймой\nРаспустились кисти\nБелой бахромой.",
                    "Под моим окном\nПринакрылась снегом,\nТочно серебром.\n\nНа пушистых ветках\nСнежною каймой\nРаспустились кисти\nБелой бахромой.",
                    "Сергей Есенин",
                    1,
                    25,
                    10));
                    
                texts.add(new Text(poemsCategoryId,
                    "Зимнее утро",
                    "Мороз и солнце; день чудесный!\nЕщё ты дремлешь, друг прелестный —\nПора, красавица, проснись:\nОткрой сомкнуты негой взоры\nНавстречу северной Авроры,\nЗвездою севера явись!",
                    "Мороз и солнце; день чудесный!\nЕщё ты дремлешь, друг прелестный —\nПора, красавица, проснись:\nОткрой сомкнуты негой взоры\nНавстречу северной Авроры,\nЗвездою севера явись!",
                    "Александр Пушкин",
                    2,
                    30,
                    15));
                    
                texts.add(new Text(poemsCategoryId,
                    "У лукоморья дуб зелёный",
                    "У лукоморья дуб зелёный;\nЗлатая цепь на дубе том:\nИ днём и ночью кот учёный\nВсё ходит по цепи кругом;\nИдёт направо — песнь заводит,\nНалево — сказку говорит.",
                    "У лукоморья дуб зелёный;\nЗлатая цепь на дубе том:\nИ днём и ночью кот учёный\nВсё ходит по цепи кругом;\nИдёт направо — песнь заводит,\nНалево — сказку говорит.",
                    "Александр Пушкин",
                    3,
                    35,
                    20));
            }
            
            // Рассказы
            if (storiesCategoryId > 0) {
                texts.add(new Text(storiesCategoryId,
                    "Воробей",
                    "Я возвращался с охоты и шёл по аллее сада. Собака бежала впереди меня. Вдруг она уменьшила свои шаги и начала красться, как бы зачуяв перед собою дичь. Я глянул вдоль аллеи и увидел молодого воробья с желтизной около клюва и пухом на голове. Он упал из гнезда и сидел неподвижно, беспомощно растопырив едва прораставшие крылышки.",
                    "Я возвращался с охоты и шёл по аллее сада. Собака бежала впереди меня. Вдруг она уменьшила свои шаги и начала красться, как бы зачуяв перед собою дичь. Я глянул вдоль аллеи и увидел молодого воробья с желтизной около клюва и пухом на голове. Он упал из гнезда и сидел неподвижно, беспомощно растопырив едва прораставшие крылышки.",
                    "Иван Тургенев",
                    1,
                    50,
                    12));
                    
                texts.add(new Text(storiesCategoryId,
                    "Каштанка",
                    "Молодая рыжая собака — помесь такса с дворняжкой — очень похожая мордой на лисицу, бегала взад и вперёд по тротуару и беспокойно оглядывалась по сторонам. Изредка она останавливалась и, плача, приподнимая то одну озябшую лапу, то другую, старалась дать себе отчёт: как это могло случиться, что она заблудилась?",
                    "Молодая рыжая собака — помесь такса с дворняжкой — очень похожая мордой на лисицу, бегала взад и вперёд по тротуару и беспокойно оглядывалась по сторонам. Изредка она останавливалась и, плача, приподнимая то одну озябшую лапу, то другую, старалась дать себе отчёт: как это могло случиться, что она заблудилась?",
                    "Антон Чехов",
                    2,
                    60,
                    18));
            }
            
            // Басни
            if (fablesCategoryId > 0) {
                texts.add(new Text(fablesCategoryId,
                    "Ворона и Лисица",
                    "Уж сколько раз твердили миру,\nЧто лесть гнусна, вредна; но только всё не впрок,\nИ в сердце льстец всегда отыщет уголок.\n\nВороне где-то бог послал кусочек сыру;\nНа ель Ворона взгромоздясь,\nПозавтракать было совсем уж собралась,\nДа призадумалась, а сыр во рту держала.\nНа ту беду Лиса близехонько бежала.",
                    "Уж сколько раз твердили миру,\nЧто лесть гнусна, вредна; но только всё не впрок,\nИ в сердце льстец всегда отыщет уголок.\n\nВороне где-то бог послал кусочек сыру;\nНа ель Ворона взгромоздясь,\nПозавтракать было совсем уж собралась,\nДа призадумалась, а сыр во рту держала.\nНа ту беду Лиса близехонько бежала.",
                    "Иван Крылов",
                    2,
                    55,
                    15));
                    
                texts.add(new Text(fablesCategoryId,
                    "Стрекоза и Муравей",
                    "Попрыгунья Стрекоза\nЛето красное пропела;\nОглянуться не успела,\nКак зима катит в глаза.\nПомертвело чисто поле;\nНет уж дней тех светлых боле,\nКак под каждым ей листком\nБыл готов и стол и дом.",
                    "Попрыгунья Стрекоза\nЛето красное пропела;\nОглянуться не успела,\nКак зима катит в глаза.\nПомертвело чисто поле;\nНет уж дней тех светлых боле,\nКак под каждым ей листком\nБыл готов и стол и дом.",
                    "Иван Крылов",
                    1,
                    40,
                    10));
            }
            
            if (!texts.isEmpty()) {
                textDao.insertTexts(texts);
                // После вставки получаем тексты с правильными ID из БД
                texts = textDao.getAllTexts();
            }
            
            return texts;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }
    
    private static void initializeQuestions(QuestionDao questionDao, List<Text> texts) {
        try {
            // Проверяем, есть ли уже вопросы
            if (texts.isEmpty()) {
                return; // Нет текстов - не создаем вопросы
            }
            
            // Проверяем, есть ли уже вопросы для первого текста
            Text firstText = texts.get(0);
            if (firstText.id == 0) {
                // Тексты еще не вставлены в БД - пропускаем создание вопросов
                return;
            }
            
            List<Question> existingQuestions = questionDao.getAllQuestionsByTextId(firstText.id);
            if (!existingQuestions.isEmpty()) {
                return; // Вопросы уже есть
            }
            
            List<Question> questions = new ArrayList<>();
            
            // Находим тексты по названиям (используем тексты из БД, а не из списка)
            Text vorbeyText = null;
            Text kashankaText = null;
            Text voronaText = null;
            Text strekozaText = null;
            
            for (Text text : texts) {
                // Убеждаемся, что текст уже в БД (id > 0)
                if (text.id == 0) continue;
                
                if ("Воробей".equals(text.title)) vorbeyText = text;
                else if ("Каштанка".equals(text.title)) kashankaText = text;
                else if ("Ворона и Лисица".equals(text.title)) voronaText = text;
                else if ("Стрекоза и Муравей".equals(text.title)) strekozaText = text;
            }
            
            // Вопросы для "Воробей"
            if (vorbeyText != null) {
                questions.add(new Question(vorbeyText.id,
                    "Кто был главным героем рассказа?",
                    "Воробей",
                    "Собака",
                    "Охотник",
                    "Кошка"));
                
                questions.add(new Question(vorbeyText.id,
                    "Где находился воробей?",
                    "В саду",
                    "В лесу",
                    "В парке",
                    "На улице"));
            }
            
            // Вопросы для "Каштанка"
            if (kashankaText != null) {
                questions.add(new Question(kashankaText.id,
                    "Какого цвета была собака?",
                    "Рыжая",
                    "Чёрная",
                    "Белая",
                    "Серая"));
                
                questions.add(new Question(kashankaText.id,
                    "Что случилось с собакой?",
                    "Она заблудилась",
                    "Она потеряла хозяина",
                    "Она убежала",
                    "Она заболела"));
            }
            
            // Вопросы для "Ворона и Лисица"
            if (voronaText != null) {
                questions.add(new Question(voronaText.id,
                    "Что держала Ворона во рту?",
                    "Сыр",
                    "Хлеб",
                    "Мясо",
                    "Орех"));
                
                questions.add(new Question(voronaText.id,
                    "Кто пытался забрать еду у Вороны?",
                    "Лиса",
                    "Волк",
                    "Медведь",
                    "Заяц"));
            }
            
            // Вопросы для "Стрекоза и Муравей"
            if (strekozaText != null) {
                questions.add(new Question(strekozaText.id,
                    "Какое время года наступило?",
                    "Зима",
                    "Весна",
                    "Лето",
                    "Осень"));
                
                questions.add(new Question(strekozaText.id,
                    "Что делала Стрекоза летом?",
                    "Пела",
                    "Работала",
                    "Спала",
                    "Играла"));
            }
            
            if (!questions.isEmpty()) {
                questionDao.insertQuestions(questions);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static void initializeTongueTwisters(TongueTwisterDao tongueTwisterDao) {
        try {
            if (tongueTwisterDao.getAllTongueTwisters().isEmpty()) {
            List<TongueTwister> tongueTwisters = new ArrayList<>();
            
            // Добавляем скороговорки из PDF файла
            tongueTwisters.add(new TongueTwister(
                "Бредут бобры",
                "Бредут бобры в сыры боры. Бобры храбры, а для бобрят добры.",
                "б, р",
                2));
                
            tongueTwisters.add(new TongueTwister(
                "Все бобры добры",
                "Все бобры добры для своих бобрят.",
                "б, р",
                1));
                
            tongueTwisters.add(new TongueTwister(
                "Белый снег",
                "Белый снег, белый мел, белый заяц тоже бел. А вот белка не бела – белой даже не была.",
                "б",
                2));
                
            tongueTwisters.add(new TongueTwister(
                "Бык тупогуб",
                "Бык тупогуб, тупогубенький бычок, у быка бела губа была тупа.",
                "б, п",
                2));
                
            tongueTwisters.add(new TongueTwister(
                "Водовоз",
                "Водовоз вёз воду из-под водопровода.",
                "в, п",
                1));
                
            tongueTwisters.add(new TongueTwister(
                "Свиристель",
                "Свиристель свиристит свирелью.",
                "в, с",
                1));
                
            tongueTwisters.add(new TongueTwister(
                "Тридцать три корабля",
                "Тридцать три корабля лавировали, лавировали, да не вылавировали.",
                "в, т, р",
                3));
                
            tongueTwisters.add(new TongueTwister(
                "Выдра у выдры",
                "Выдра у выдры норовила вырвать рыбу.",
                "в, р",
                2));
                
            tongueTwisters.add(new TongueTwister(
                "Дятел",
                "Дятел дуб долбил, долбил, продалбливал, да не продолбил и не выдолбил.",
                "д, б, л",
                2));
                
            tongueTwisters.add(new TongueTwister(
                "Два дровосека",
                "Два дровосека, два дроворуба, два дровокола говорили про Ларьку, про Варьку, про Ларину жену.",
                "д, р",
                2));
                
            tongueTwisters.add(new TongueTwister(
                "У ежа ежата",
                "У ежа ежата, у ужа ужата.",
                "ж",
                1));
                
            tongueTwisters.add(new TongueTwister(
                "Жужжит жужелица",
                "Жужжит-жужжит жужелица, жужжит да кружится. Говорю ей, не жужжи, не кружись, а ты лучше спать ложись. Всех соседей перебудишь, коль жужжать под ухом будешь.",
                "ж",
                3));
                
            tongueTwisters.add(new TongueTwister(
                "Ярослав и Ярославна",
                "Ярослав и Ярославна поселились в Ярославле. В Ярославле живут славно Ярослав и Ярославна.",
                "й, р, в",
                2));
                
            tongueTwisters.add(new TongueTwister(
                "Все скороговорки",
                "Все скороговорки не перевыскороговоришь.",
                "к, в",
                2));
                
            tongueTwisters.add(new TongueTwister(
                "У Кондрата",
                "У Кондрата куртка коротковата.",
                "к, т, р",
                2));
                
            tongueTwisters.add(new TongueTwister(
                "Идет с козой",
                "Идет с козой косой козел.",
                "к, з, с",
                2));
                
            tongueTwisters.add(new TongueTwister(
                "Колотил Клим",
                "Колотил Клим в один блин клин.",
                "к, л",
                1));
                
            tongueTwisters.add(new TongueTwister(
                "Краб крабу",
                "Краб крабу сделал грабли, подарил грабли крабу – грабъ граблями гравий, краб.",
                "к, р, г",
                3));
                
            tongueTwisters.add(new TongueTwister(
                "Кукушка",
                "Кукушка кукушонку купила капюшон, надел кукушонок капюшон, в капюшоне кукушонок смешон.",
                "к, ш, п, н",
                2));
                
            tongueTwisters.add(new TongueTwister(
                "Карл украл",
                "Карл украл у Клары кораллы, а Клара украла у Карла кларнет.",
                "к, р, л",
                3));
                
            tongueTwisters.add(new TongueTwister(
                "Королева",
                "Королева кавалеру подарила каравеллу.",
                "к, р, в, л",
                2));
                
            tongueTwisters.add(new TongueTwister(
                "Курьера курьер",
                "Курьера курьер обгоняет в карьер.",
                "к, р",
                2));
                
            tongueTwisters.add(new TongueTwister(
                "Коси коса",
                "Коси, коса, пока роса, роса долой – и мы домой.",
                "к, с",
                1));
                
            tongueTwisters.add(new TongueTwister(
                "Наш Полкан",
                "Наш Полкан из Байкала лакал. Лакал Полкан, лакал, да не мелел Байкал.",
                "к, л, б",
                2));
                
            tongueTwisters.add(new TongueTwister(
                "Около колодца",
                "Около колодца кольцо не найдётся.",
                "к, л, ц",
                2));
                
            tongueTwisters.add(new TongueTwister(
                "Шла Саша",
                "Шла Саша по шоссе, несла сушку на шесте и сосала сушку.",
                "ш, с",
                2));
                
            tongueTwisters.add(new TongueTwister(
                "Шесть мышат",
                "Шесть мышат в камышах шуршат.",
                "ш",
                1));
                
            tongueTwisters.add(new TongueTwister(
                "Два щенка",
                "Два щенка щека к щеке щиплют щеку в уголке.",
                "ш, к",
                3));
                
            tongueTwisters.add(new TongueTwister(
                "Саша шустро",
                "Саша шустро сушит сушки. Сушек высушил штук шесть. И смешно спешат старушки Сушек Сашиных поесть.",
                "ш, с",
                2));
                
            tongueTwisters.add(new TongueTwister(
                "Тщетно тщится",
                "Тщетно тщится щука ущемить леща.",
                "щ, т",
                2));
                
            tongueTwisters.add(new TongueTwister(
                "Щеночкам",
                "Щеночкам щеточками чистили щечки.",
                "щ, ч",
                2));
                
            tongueTwisters.add(new TongueTwister(
                "Волки рыщут",
                "Волки рыщут – пищу ищут.",
                "щ, т",
                1));
                
            tongueTwisterDao.insertTongueTwisters(tongueTwisters);
        }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static void initializeShopItems(ShopItemDao shopItemDao) {
        try {
            if (shopItemDao.getAllShopItems().isEmpty()) {
            List<ShopItem> shopItems = new ArrayList<>();
            
            // Фоны
            shopItems.add(new ShopItem("Синий фон", "Классический синий фон", 50, 
                android.R.drawable.ic_menu_gallery));
            shopItems.add(new ShopItem("Звездный фон", "Загадочный звездный фон", 75, 
                android.R.drawable.ic_menu_gallery));
            shopItems.add(new ShopItem("Красный фон", "Энергичный красный фон", 100, 
                android.R.drawable.ic_menu_gallery));
            shopItems.add(new ShopItem("Фиолетовый фон", "Мистический фиолетовый фон", 150, 
                android.R.drawable.ic_menu_gallery));
            
            shopItemDao.insertShopItems(shopItems);
        }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

