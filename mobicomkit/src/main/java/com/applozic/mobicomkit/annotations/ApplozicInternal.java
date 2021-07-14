package com.applozic.mobicomkit.annotations;

public @interface ApplozicInternal {
    AppliesTo[] appliesTo() default AppliesTo.SPECIFIED_MEMBERS; //use if this annotation is applied to a class

    enum AppliesTo {
        SPECIFIED_MEMBERS, ALL_MEMBERS, STATIC_MEMBERS, INSTANCE_MEMBERS, STATIC_METHODS, INSTANCE_METHODS, STATIC_VARIABLES, INSTANCE_VARIABLES;
    }
}
