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
              "question": "What is the speaker's name?",
              "type": "multiple_choice",
              "options": ["John", "Alex", "Maria"],
              "correctAnswer": 1,
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
              "question": "What time does the person wake up?",
              "type": "multiple_choice",
              "options": ["7 AM", "8 AM", "9 AM"],
              "correctAnswer": 0,
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
              "question": "What is the first step in writing a professional email?",
              "type": "multiple_choice",
              "options": ["Write the main content", "Use a clear subject line", "Choose a closing"],
              "correctAnswer": 1,
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
              "question": "How do people typically greet each other in Japan?",
              "type": "multiple_choice",
              "options": ["Handshake", "Hug", "Bow"],
              "correctAnswer": 2,
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
              "question": "What was the primary driver of increased consumer spending in Q4?",
              "type": "multiple_choice",
              "options": ["Government stimulus", "Holiday sales", "Lower interest rates"],
              "correctAnswer": 1,
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
              "question": "What is the main purpose of the body paragraphs?",
              "type": "multiple_choice",
              "options": ["To introduce the topic", "To develop specific points supporting the thesis", "To summarize the essay"],
              "correctAnswer": 1,
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
          "duration": 45,
          "transcript": "Cashier: Hello, what can I get for you?\\nCustomer: I'd like a cheeseburger and a small fries, please.\\nCashier: Anything to drink?\\nCustomer: Just a water.",
          "questions": [
            {
              "question": "What did the customer order to eat?",
              "type": "multiple_choice",
              "options": ["A salad", "A cheeseburger and fries", "A chicken sandwich"],
              "correctAnswer": 1,
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
          "duration": 60,
          "transcript": "Receptionist: Good morning, Grand Hotel. How can I help you?\\nCustomer: Hello, I'd like to book a room for two nights, please.\\nReceptionist: Certainly. For which dates?\\nCustomer: For the 14th and 15th of March.",
          "questions": [
            {
              "question": "For how many nights does the customer want to book a room?",
              "type": "multiple_choice",
              "options": ["One night", "Two nights", "Three nights"],
              "correctAnswer": 1,
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
          "duration": 90,
          "transcript": "Professor: Today, we will delve into the socio-economic implications of post-war industrialization. A pivotal aspect to consider is the unprecedented urbanization that occurred during this period.",
          "questions": [
            {
              "question": "What is the main topic of the lecture?",
              "type": "multiple_choice",
              "options": ["Agricultural revolution", "Socio-economic effects of industrialization", "20th-century art movements"],
              "correctAnswer": 1,
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
              "question": "Which sentence is correct?",
              "type": "multiple_choice",
              "options": ["They is happy.", "She are happy.", "He is happy."],
              "correctAnswer": 2,
              "points": 10
            },
            {
              "question": "What is the plural of 'child'?",
              "type": "multiple_choice",
              "options": ["Childs", "Children", "Childes"],
              "correctAnswer": 1,
              "points": 10
            }
          ],
          "passingScore": 70
        }
        """;
  }

  private String createQuizContent2() {
    return """
        {
          "questions": [
            {
              "question": "I ___ to the cinema yesterday.",
              "type": "multiple_choice",
              "options": ["go", "gone", "went"],
              "correctAnswer": 2,
              "points": 10
            },
            {
              "question": "She has ___ her homework.",
              "type": "multiple_choice",
              "options": ["do", "did", "done"],
              "correctAnswer": 2,
              "points": 10
            }
          ],
          "passingScore": 70
        }
        """;
  }

  private String createQuizContent3() {
    return """
        {
          "questions": [
            {
              "question": "What does the idiom 'hit the nail on the head' mean?",
              "type": "multiple_choice",
              "options": ["To be exactly right", "To make a mistake", "To work hard"],
              "correctAnswer": 0,
              "points": 10
            },
            {
              "question": "If a project is 'on the back burner', it is...",
              "type": "multiple_choice",
              "options": ["Finished", "A high priority", "Not a priority right now"],
              "correctAnswer": 2,
              "points": 10
            }
          ],
          "passingScore": 75
        }
        """;
  }

  private String createSpeakingContent1() {
    return """
        {
          "scenario": "You meet someone for the first time at a party. Introduce yourself and ask them about themselves.",
          "difficulty": "beginner",
          "prompts": [
            {
              "prompt": "Greet the person politely and share your name.",
              "sampleAnswers": ["Hello! I'm Alex.", "Hi, my name is Jamie."]
            },
            {
              "prompt": "Ask where they are from or what they do.",
              "sampleAnswers": ["Where are you from?", "What do you do for work?"]
            }
          ],
          "rolePlaySettings": {
            "turns": 3
          }
        }
        """;
  }

  private String createSpeakingContent2() {
    return """
        {
          "scenario": "You are lost in a new city. Ask a stranger for directions to the nearest train station.",
          "difficulty": "beginner",
          "prompts": [
            {
              "prompt": "Politely get the person's attention.",
              "sampleAnswers": ["Excuse me, could you help me?", "Hi there, may I ask you something?"]
            },
            {
              "prompt": "Ask for directions to the train station.",
              "sampleAnswers": ["How do I get to the nearest train station?", "Could you tell me where the train station is?"]
            },
            {
              "prompt": "Confirm the directions you received.",
              "sampleAnswers": ["So I turn left at the bank and go straight?", "Just to make sure, is it past the museum?"]
            }
          ],
          "rolePlaySettings": {
            "turns": 4
          }
        }
        """;
  }

  private String createSpeakingContent3() {
    return """
        {
          "scenario": "You are in a job interview. The interviewer asks, 'Tell me about your strengths.' Respond to the question.",
          "difficulty": "intermediate",
          "prompts": [
            {
              "prompt": "Introduce your strongest professional skill.",
              "sampleAnswers": ["One of my key strengths is problem-solving.", "I'm very strong at collaborating across teams."]
            },
            {
              "prompt": "Provide an example that demonstrates this strength.",
              "sampleAnswers": ["In my last role, I led a project that reduced onboarding time by 20%."]
            },
            {
              "prompt": "Explain how this strength will help the company.",
              "sampleAnswers": ["This means I can help streamline your onboarding process right away."]
            }
          ],
          "rolePlaySettings": {
            "turns": 5
          }
        }
        """;
  }

  private String createSpeakingContent4() {
    return """
        {
          "scenario": "You are making small talk with a new colleague. Ask them about their hobbies and share one of your own.",
          "difficulty": "intermediate",
          "prompts": [
            {
              "prompt": "Start the conversation with a friendly opener.",
              "sampleAnswers": ["It's nice to finally meet you!", "How are you settling into the team?"]
            },
            {
              "prompt": "Ask about their hobbies.",
              "sampleAnswers": ["What do you like to do in your free time?", "Do you have any hobbies?"]
            },
            {
              "prompt": "Share one of your hobbies and relate it to theirs.",
              "sampleAnswers": ["I love hiking on weekends too!", "I usually read sci-fi novels—have you tried any recently?"]
            }
          ],
          "rolePlaySettings": {
            "turns": 4
          }
        }
        """;
  }

  private String createSpeakingContent5() {
    return """
        {
          "scenario": "You are in a business meeting to negotiate the price of a service. Your goal is to get a 10% discount. Make your opening statement.",
          "difficulty": "advanced",
          "prompts": [
            {
              "prompt": "State your appreciation for the partnership and set a positive tone.",
              "sampleAnswers": ["We appreciate the results you've delivered so far and value this partnership."]
            },
            {
              "prompt": "Present your case for a discount with supporting data.",
              "sampleAnswers": ["Given the three-year commitment and volume we're bringing, we are looking for a 10% discount."]
            },
            {
              "prompt": "Suggest how the discount benefits both sides.",
              "sampleAnswers": ["This adjustment would let us expand the contract scope next quarter, which benefits both teams."]
            }
          ],
          "rolePlaySettings": {
            "turns": 8
          }
        }
        """;
  }

  private String createSpeakingContent6() {
    return """
        {
          "scenario": "You are in a debate. The topic is 'Technology does more harm than good.' Argue against the motion (i.e., argue that technology does more good).",
          "difficulty": "advanced",
          "prompts": [
            {
              "prompt": "Open with a confident rebuttal statement.",
              "sampleAnswers": ["On the contrary, recent data shows technology saves more lives than ever before."]
            },
            {
              "prompt": "Provide two supporting arguments.",
              "sampleAnswers": ["Consider medical innovations and remote education—both rely on technology."]
            },
            {
              "prompt": "Acknowledge a counterpoint and refute it.",
              "sampleAnswers": ["While it's true that technology can be distracting, its benefits for global collaboration outweigh the drawbacks."]
            }
          ],
          "rolePlaySettings": {
            "turns": 6
          }
        }
        """;
  }
}
