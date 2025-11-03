package com.lexia.backend.seeder;

import com.lexia.backend.entity.Course;
import com.lexia.backend.entity.Lesson;
import com.lexia.backend.entity.Section;
import com.lexia.backend.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class CourseSeeder implements ApplicationRunner {

    private final CourseRepository courseRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (courseRepository.count() == 0) {
            log.info("No courses found in DB. Seeding initial data...");
            createCourses();
            log.info("Finished seeding initial course data. Created {} courses.", courseRepository.count());
        } else {
            log.info("Database already contains course data. Skipping seeding.");
        }
    }

    private void createCourses() {
        Course course1 = createCourseA1();
        Course course2 = createCourseB1();
        Course course3 = createCourseC1();

        courseRepository.saveAll(List.of(course1, course2, course3));
    }

    private Course createCourseA1() {
        Course course = Course.builder()
                .title("English Basics (A1)")
                .description(
                        "Master the fundamentals of English for everyday communication. This course covers basic grammar, vocabulary, and conversational skills.")
                .cefrLevel("A1")
                .thumbnailUrl("https://i.imgur.com/5d2tGgU.png")
                .isPublished(true)
                .build();

        Section section1 = createSection(course, "Getting Started", 0);
        section1.addLesson(createLesson(section1, "Alphabet and Greetings", 0, Lesson.LessonType.READING, 5,
                createReadingContent1()));
        section1.addLesson(createLesson(section1, "Basic Introductions", 1, Lesson.LessonType.SPEAKING, 10,
                createSpeakingContent1()));
        section1.addLesson(createLesson(section1, "Numbers and Plurals Quiz", 2, Lesson.LessonType.QUIZ, 15,
                createQuizContent1()));

        Section section2 = createSection(course, "Daily Conversations", 1);
        section2.addLesson(
                createLesson(section2, "Ordering Food", 0, Lesson.LessonType.LISTENING, 10, createListeningContent1()));
        section2.addLesson(createLesson(section2, "Asking for Directions", 1, Lesson.LessonType.SPEAKING, 15,
                createSpeakingContent2()));
        section2.addLesson(
                createLesson(section2, "Daily Routines", 2, Lesson.LessonType.READING, 10, createReadingContent2()));

        course.addSection(section1);
        course.addSection(section2);
        return course;
    }

    private Course createCourseB1() {
        Course course = Course.builder()
                .title("Intermediate English (B1)")
                .description(
                        "Expand your English skills for work and social situations. Focus on more complex grammar, expressing opinions, and understanding native speakers.")
                .cefrLevel("B1")
                .thumbnailUrl("https://i.imgur.com/6y3tHjV.png")
                .isPublished(true)
                .build();

        Section section1 = createSection(course, "Work and Career", 0);
        section1.addLesson(createLesson(section1, "Writing a Professional Email", 0, Lesson.LessonType.READING, 15,
                createReadingContent3()));
        section1.addLesson(createLesson(section1, "Job Interview Practice", 1, Lesson.LessonType.SPEAKING, 20,
                createSpeakingContent3()));
        section1.addLesson(
                createLesson(section1, "Past Tenses Review", 2, Lesson.LessonType.QUIZ, 10, createQuizContent2()));

        Section section2 = createSection(course, "Travel and Culture", 1);
        section2.addLesson(createLesson(section2, "Booking a Hotel", 0, Lesson.LessonType.LISTENING, 15,
                createListeningContent2()));
        section2.addLesson(createLesson(section2, "Discussing Hobbies", 1, Lesson.LessonType.SPEAKING, 15,
                createSpeakingContent4()));
        section2.addLesson(createLesson(section2, "Cultural Etiquette", 2, Lesson.LessonType.READING, 10,
                createReadingContent4()));

        course.addSection(section1);
        course.addSection(section2);
        return course;
    }

    private Course createCourseC1() {
        Course course = Course.builder()
                .title("Advanced English (C1)")
                .description(
                        "Achieve fluency and confidence in professional and academic settings. This course covers nuanced expression, advanced vocabulary, and complex text analysis.")
                .cefrLevel("C1")
                .thumbnailUrl("https://i.imgur.com/7E4tFjW.png")
                .isPublished(false) // Not published by default
                .build();

        Section section1 = createSection(course, "Business English", 0);
        section1.addLesson(createLesson(section1, "Negotiating a Deal", 0, Lesson.LessonType.SPEAKING, 25,
                createSpeakingContent5()));
        section1.addLesson(createLesson(section1, "Analyzing Market Trends", 1, Lesson.LessonType.READING, 20,
                createReadingContent5()));
        section1.addLesson(createLesson(section1, "Advanced Business Idioms", 2, Lesson.LessonType.QUIZ, 15,
                createQuizContent3()));

        Section section2 = createSection(course, "Academic Writing", 1);
        section2.addLesson(createLesson(section2, "Structuring an Essay", 0, Lesson.LessonType.READING, 20,
                createReadingContent6()));
        section2.addLesson(createLesson(section2, "Understanding Academic Lectures", 1, Lesson.LessonType.LISTENING, 25,
                createListeningContent3()));
        section2.addLesson(createLesson(section2, "Debating Complex Topics", 2, Lesson.LessonType.SPEAKING, 30,
                createSpeakingContent6()));

        course.addSection(section1);
        course.addSection(section2);
        return course;
    }

    private Section createSection(Course course, String title, int orderIndex) {
        return Section.builder()
                .course(course)
                .title(title)
                .orderIndex(orderIndex)
                .build();
    }

    private Lesson createLesson(Section section, String title, int orderIndex, Lesson.LessonType type, int duration,
            String content) {
        return Lesson.builder()
                .section(section)
                .title(title)
                .orderIndex(orderIndex)
                .lessonType(type)
                .durationMinutes(duration)
                .content(content)
                .build();
    }

    // --- Sample Lesson Content ---

    private String createReadingContent1() {
        return """
                {
                  "passages": [
                    {
                      "title": "Greetings",
                      "text": "Hello! My name is Alex. What is your name?"
                    }
                  ],
                  "questions": [
                    {
                      "questionText": "What is the speaker's name?",
                      "options": ["John", "Alex", "Maria"],
                      "correctOptionIndex": 1,
                      "points": 10
                    }
                  ],
                  "vocabulary": [
                    {"word": "Hello", "definition": "A common greeting."},
                    {"word": "Name", "definition": "What someone is called."}
                  ]
                }
                """;
    }

    private String createReadingContent2() {
        return """
                {
                  "passages": [
                    {
                      "title": "My Daily Routine",
                      "text": "I wake up at 7 AM every morning. I brush my teeth and have breakfast. I go to work at 8:30 AM."
                    }
                  ],
                  "questions": [
                    {
                      "questionText": "What time does the person wake up?",
                      "options": ["7 AM", "8 AM", "9 AM"],
                      "correctOptionIndex": 0,
                      "points": 10
                    }
                  ]
                }
                """;
    }

    private String createReadingContent3() {
        return """
                {
                  "passages": [
                    {
                      "title": "Professional Email Etiquette",
                      "text": "When writing a professional email, always use a clear subject line. Start with a formal greeting, such as 'Dear Mr. Smith,'. State your purpose clearly and concisely. End with a professional closing like 'Sincerely,' or 'Best regards,'."
                    }
                  ],
                  "questions": [
                    {
                      "questionText": "What is the first step in writing a professional email?",
                      "options": ["Write the main content", "Use a clear subject line", "Choose a closing"],
                      "correctOptionIndex": 1,
                      "points": 10
                    }
                  ]
                }
                """;
    }

    private String createReadingContent4() {
        return """
                {
                  "passages": [
                    {
                      "title": "Cultural Differences in Greetings",
                      "text": "In Japan, people often bow as a greeting. In France, it is common to kiss on both cheeks. In the United States, a firm handshake is the standard."
                    }
                  ],
                  "questions": [
                    {
                      "questionText": "How do people typically greet each other in Japan?",
                      "options": ["Handshake", "Hug", "Bow"],
                      "correctOptionIndex": 2,
                      "points": 10
                    }
                  ]
                }
                """;
    }

    private String createReadingContent5() {
        return """
                {
                  "passages": [
                    {
                      "title": "Q4 Market Analysis",
                      "text": "The fourth quarter showed a significant uptick in consumer spending, driven by holiday sales and improved economic sentiment. The tech sector, in particular, outperformed expectations with a 15% growth year-over-year. However, supply chain disruptions remain a key concern for the upcoming fiscal year."
                    }
                  ],
                  "questions": [
                    {
                      "questionText": "What was the primary driver of increased consumer spending in Q4?",
                      "options": ["Government stimulus", "Holiday sales", "Lower interest rates"],
                      "correctOptionIndex": 1,
                      "points": 10
                    }
                  ]
                }
                """;
    }

    private String createReadingContent6() {
        return """
                {
                  "passages": [
                    {
                      "title": "The Five-Paragraph Essay Structure",
                      "text": "A standard academic essay follows a five-paragraph structure: an introduction with a thesis statement, three body paragraphs each developing a single point, and a conclusion that summarizes the arguments and restates the thesis."
                    }
                  ],
                  "questions": [
                    {
                      "questionText": "What is the main purpose of the body paragraphs?",
                      "options": ["To introduce the topic", "To develop specific points supporting the thesis", "To summarize the essay"],
                      "correctOptionIndex": 1,
                      "points": 10
                    }
                  ]
                }
                """;
    }

    private String createListeningContent1() {
        return """
                {
                  "audioUrl": "https://storage.googleapis.com/lexia-assets/audio/a1-ordering-food.mp3",
                  "durationSeconds": 45,
                  "transcript": [
                    {"speaker": "Cashier", "line": "Hello, what can I get for you?", "timestamp": 2},
                    {"speaker": "Customer", "line": "Hi, I'd like a cheeseburger and a small fries, please.", "timestamp": 5},
                    {"speaker": "Cashier", "line": "Anything to drink?", "timestamp": 8},
                    {"speaker": "Customer", "line": "Just a water.", "timestamp": 10}
                  ],
                  "questions": [
                    {
                      "questionText": "What did the customer order to eat?",
                      "options": ["A salad", "A cheeseburger and fries", "A chicken sandwich"],
                      "correctOptionIndex": 1,
                      "points": 10
                    }
                  ]
                }
                """;
    }

    private String createListeningContent2() {
        return """
                {
                  "audioUrl": "https://storage.googleapis.com/lexia-assets/audio/b1-booking-hotel.mp3",
                  "durationSeconds": 60,
                  "transcript": [
                    {"speaker": "Receptionist", "line": "Good morning, Grand Hotel. How can I help you?", "timestamp": 3},
                    {"speaker": "Customer", "line": "Hello, I'd like to book a room for two nights, please.", "timestamp": 7},
                    {"speaker": "Receptionist", "line": "Certainly. For which dates?", "timestamp": 10},
                    {"speaker": "Customer", "line": "For the 14th and 15th of March.", "timestamp": 13}
                  ],
                  "questions": [
                    {
                      "questionText": "For how many nights does the customer want to book a room?",
                      "options": ["One night", "Two nights", "Three nights"],
                      "correctOptionIndex": 1,
                      "points": 10
                    }
                  ]
                }
                """;
    }

    private String createListeningContent3() {
        return """
                {
                  "audioUrl": "https://storage.googleapis.com/lexia-assets/audio/c1-academic-lecture.mp3",
                  "durationSeconds": 90,
                  "transcript": [
                    {"speaker": "Professor", "line": "Today, we will delve into the socio-economic implications of post-war industrialization. A pivotal aspect to consider is the unprecedented urbanization that occurred during this period.", "timestamp": 5}
                  ],
                  "questions": [
                    {
                      "questionText": "What is the main topic of the lecture?",
                      "options": ["Agricultural revolution", "Socio-economic effects of industrialization", "20th-century art movements"],
                      "correctOptionIndex": 1,
                      "points": 10
                    }
                  ]
                }
                """;
    }

    private String createQuizContent1() {
        return """
                {
                  "questions": [
                    {
                      "questionText": "Which sentence is correct?",
                      "options": ["They is happy.", "She are happy.", "He is happy."],
                      "correctOptionIndex": 2,
                      "points": 10
                    },
                    {
                      "questionText": "What is the plural of 'child'?",
                      "options": ["Childs", "Children", "Childes"],
                      "correctOptionIndex": 1,
                      "points": 10
                    }
                  ]
                }
                """;
    }

    private String createQuizContent2() {
        return """
                {
                  "questions": [
                    {
                      "questionText": "I ___ to the cinema yesterday.",
                      "options": ["go", "gone", "went"],
                      "correctOptionIndex": 2,
                      "points": 10
                    },
                    {
                      "questionText": "She has ___ her homework.",
                      "options": ["do", "did", "done"],
                      "correctOptionIndex": 2,
                      "points": 10
                    }
                  ]
                }
                """;
    }

    private String createQuizContent3() {
        return """
                {
                  "questions": [
                    {
                      "questionText": "What does the idiom 'hit the nail on the head' mean?",
                      "options": ["To be exactly right", "To make a mistake", "To work hard"],
                      "correctOptionIndex": 0,
                      "points": 10
                    },
                    {
                      "questionText": "If a project is 'on the back burner', it is...",
                      "options": ["Finished", "A high priority", "Not a priority right now"],
                      "correctOptionIndex": 2,
                      "points": 10
                    }
                  ]
                }
                """;
    }

    private String createSpeakingContent1() {
        return """
                {
                  "scenario": "You meet someone for the first time at a party. Introduce yourself and ask them about themselves.",
                  "keyVocabulary": ["introduce", "from", "work as"],
                  "suggestedTurns": 3
                }
                """;
    }

    private String createSpeakingContent2() {
        return """
                {
                  "scenario": "You are lost in a new city. Ask a stranger for directions to the nearest train station.",
                  "keyVocabulary": ["excuse me", "how do I get to", "nearest", "train station"],
                  "suggestedTurns": 4
                }
                """;
    }

    private String createSpeakingContent3() {
        return """
                {
                  "scenario": "You are in a job interview. The interviewer asks, 'Tell me about your strengths.' Respond to the question.",
                  "keyVocabulary": ["strength", "team player", "problem-solving", "experience"],
                  "suggestedTurns": 5
                }
                """;
    }

    private String createSpeakingContent4() {
        return """
                {
                  "scenario": "You are making small talk with a new colleague. Ask them about their hobbies and share one of your own.",
                  "keyVocabulary": ["hobbies", "in my free time", "enjoy", "what about you"],
                  "suggestedTurns": 4
                }
                """;
    }

    private String createSpeakingContent5() {
        return """
                {
                  "scenario": "You are in a business meeting to negotiate the price of a service. Your goal is to get a 10% discount. Make your opening statement.",
                  "keyVocabulary": ["proposal", "flexible", "mutually beneficial", "long-term partnership"],
                  "suggestedTurns": 8
                }
                """;
    }

    private String createSpeakingContent6() {
        return """
                {
                  "scenario": "You are in a debate. The topic is 'Technology does more harm than good.' Argue against the motion (i.e., argue that technology does more good).",
                  "keyVocabulary": ["on the contrary", "consider the benefits", "advancements in", "while it's true that"],
                  "suggestedTurns": 6
                }
                """;
    }
}
