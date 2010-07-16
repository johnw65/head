package org.mifos.platform.questionnaire.ui.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.binding.message.MessageContext;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

public class ScreenObject implements java.io.Serializable {

    private static final long serialVersionUID = -98563977268032788L;

    private transient org.springframework.validation.Validator validator;

    @Autowired
    public void setValidator(org.springframework.validation.Validator validator) {
        this.validator = validator;
    }

    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public void validateConstraints(MessageContext context) {
        if (validator == null) {
            return;
        }

        BindingResult errors = new BeanPropertyBindingResult(this, this.getClass().getSimpleName());
        validator.validate(this, errors);
        if (errors.hasErrors()) {
            for (FieldError fieldError : errors.getFieldErrors()) {
                context.addMessage(new MessageBuilder().error().source(fieldError.getField()).codes(fieldError.getCodes()).defaultText(
                        fieldError.getDefaultMessage()).build());
            }
        }
    }

}