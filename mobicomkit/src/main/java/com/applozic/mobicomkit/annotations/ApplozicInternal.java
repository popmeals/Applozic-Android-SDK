package com.applozic.mobicomkit.annotations;

import java.lang.annotation.Documented;

@Documented
public @interface ApplozicInternal {

    /**
     * The severity level of using a <code>ApplozicInternal</code> annotated method, class or field.
     *
     * <ul>
     *     <li><code>WarningLevel.USE_WITH_CAUTION</code>: This method/field/class can be used, but it is not a part of Applozic's public API. In almost all cases, there will exist some other method/class that can solve the same purpose.</li>
     *     <li><code>WarningLevel.WILL_BREAK_CODE</code>: Calling this method (or any change to this field) WILL BREAK CODE.</li>
     * </ul>
     */
    WarningLevel warningLevel() default WarningLevel.USE_WITH_CAUTION;

    /**
     * Used to denote what members of a class the annotation applies to.
     * This is to be used if the annotation is applied to a class.
     */
    AppliesTo[] appliesTo() default AppliesTo.SPECIFIED_MEMBERS;

    enum AppliesTo {
        SPECIFIED_MEMBERS, ALL_MEMBERS, STATIC_MEMBERS, INSTANCE_MEMBERS, STATIC_METHODS, INSTANCE_METHODS, STATIC_VARIABLES, INSTANCE_VARIABLES;
    }

    enum WarningLevel {
        USE_WITH_CAUTION, DO_NOT_USE;
    }
}
