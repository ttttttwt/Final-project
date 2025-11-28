package com.lexia.backend.converter;

import com.lexia.backend.entity.Lesson.LessonType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA AttributeConverter for converting LessonType enum to/from PostgreSQL enum.
 * 
 * <p>
 * PostgreSQL uses a custom enum type 'lesson_type_enum' which requires
 * special handling when inserting/reading values. This converter ensures
 * proper type casting between Java enum and PostgreSQL enum.
 * </p>
 * 
 * @author LEXIA Team
 * @since Sprint 2
 */
@Converter(autoApply = false)
public class LessonTypeConverter implements AttributeConverter<LessonType, String> {

    /**
     * Converts the Java LessonType enum to its String representation
     * for database storage.
     *
     * @param attribute the LessonType enum value
     * @return the String name of the enum, or null if attribute is null
     */
    @Override
    public String convertToDatabaseColumn(LessonType attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.name();
    }

    /**
     * Converts the database String value back to a LessonType enum.
     *
     * @param dbData the String value from the database
     * @return the corresponding LessonType enum, or null if dbData is null
     */
    @Override
    public LessonType convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return LessonType.valueOf(dbData);
    }
}
