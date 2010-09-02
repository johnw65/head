/*
 * Copyright (c) 2005-2010 Grameen Foundation USA
 *  All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 *  implied. See the License for the specific language governing
 *  permissions and limitations under the License.
 *
 *  See also http://www.apache.org/licenses/LICENSE-2.0.html for an
 *  explanation of the license and how it is applied.
 */

package org.mifos.application.questionnaire.migration.mappers;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mifos.application.master.business.CustomFieldDefinitionEntity;
import org.mifos.application.master.business.CustomFieldType;
import org.mifos.application.util.helpers.EntityType;
import org.mifos.application.util.helpers.YesNoFlag;
import org.mifos.customers.surveys.business.QuestionUtils;
import org.mifos.customers.surveys.business.Survey;
import org.mifos.customers.surveys.business.SurveyInstance;
import org.mifos.customers.surveys.helpers.AnswerType;
import org.mifos.customers.util.helpers.CustomerLevel;
import org.mifos.framework.exceptions.ApplicationException;
import org.mifos.platform.questionnaire.persistence.SectionQuestionDao;
import org.mifos.platform.questionnaire.service.QuestionType;
import org.mifos.platform.questionnaire.service.dtos.ChoiceDto;
import org.mifos.platform.questionnaire.service.dtos.EventSourceDto;
import org.mifos.platform.questionnaire.service.dtos.QuestionDto;
import org.mifos.platform.questionnaire.service.dtos.QuestionGroupDto;
import org.mifos.platform.questionnaire.service.dtos.QuestionGroupInstanceDto;
import org.mifos.platform.questionnaire.service.dtos.QuestionGroupResponseDto;
import org.mifos.platform.questionnaire.service.dtos.SectionDto;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mifos.customers.surveys.business.SurveyUtils.getSurvey;
import static org.mifos.customers.surveys.business.SurveyUtils.getSurveyInstance;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class QuestionnaireMigrationMapperTest {

    private QuestionnaireMigrationMapper mapper;

    @Mock
    private SectionQuestionDao sectionQuestionDao;

    @Before
    public void setUp() {
        mapper = new QuestionnaireMigrationMapperImpl(sectionQuestionDao);
    }

    @Test
    public void shouldMapToQuestionDto() {
        CustomFieldDefinitionEntity customField = new CustomFieldDefinitionEntity("Favourite color", CustomerLevel.CLIENT.getValue(),
               CustomFieldType.ALPHA_NUMERIC, EntityType.CLIENT, "Red", YesNoFlag.YES);
        QuestionDto questionDto = mapper.map(customField, 0);
        assertThat(questionDto, is(notNullValue()));
        assertThat(questionDto.getTitle(), is("Favourite color"));
        assertThat(questionDto.getType(), is(QuestionType.FREETEXT));
        assertThat(questionDto.isMandatory(),is(true));
        assertThat(questionDto.getOrder(),is(0));
    }

    @Test
    public void shouldMapToQuestionGroupDto() {
        CustomFieldDefinitionEntity customField1 = new CustomFieldDefinitionEntity("CustomField1", CustomerLevel.CLIENT.getValue(),
               CustomFieldType.ALPHA_NUMERIC, EntityType.CLIENT, "Def1", YesNoFlag.YES);
        CustomFieldDefinitionEntity customField2 = new CustomFieldDefinitionEntity("CustomField2", CustomerLevel.CLIENT.getValue(),
               CustomFieldType.DATE, EntityType.CLIENT, "Def2", YesNoFlag.YES);
        CustomFieldDefinitionEntity customField3 = new CustomFieldDefinitionEntity("CustomField3", CustomerLevel.CLIENT.getValue(),
               CustomFieldType.NUMERIC, EntityType.CLIENT, "Def3", YesNoFlag.YES);
        QuestionGroupDto questionGroupDto = mapper.map(asList(customField1, customField2, customField3));
        assertThat(questionGroupDto, is(notNullValue()));
        assertThat(questionGroupDto.getTitle(), is("Additional Fields for Create Client"));
        EventSourceDto eventSourceDto = questionGroupDto.getEventSourceDto();
        assertThat(eventSourceDto, is(notNullValue()));
        assertThat(eventSourceDto.getEvent(), is("Create"));
        assertThat(eventSourceDto.getSource(), is("Client"));
        List<SectionDto> sections = questionGroupDto.getSections();
        assertThat(sections, is(notNullValue()));
        assertThat(sections.size(), is(1));
        SectionDto sectionDto = sections.get(0);
        assertThat(sectionDto.getName(), is("Misc"));
        assertThat(sectionDto.getOrder(), is(0));
        List<QuestionDto> questions = sectionDto.getQuestions();
        assertThat(questions, is(notNullValue()));
        assertQuestion(questions.get(0), "CustomField1", QuestionType.FREETEXT, 0);
        assertQuestion(questions.get(1), "CustomField2", QuestionType.DATE, 1);
        assertQuestion(questions.get(2), "CustomField3", QuestionType.NUMERIC, 2);
    }

    @Test
    public void shouldMapSurveyToQuestionGroup() {
        Survey survey = getSurvey("Sur1", "FreeText Ques");
        survey.addQuestion(QuestionUtils.getNumericQuestion("Numeric Ques", 30, 300), true);
        survey.addQuestion(QuestionUtils.getDateQuestion("Date Ques"), true);
        survey.addQuestion(QuestionUtils.getSingleSelectQuestion("Single Select Ques", "Choice1", "Choice2", "Choice3"), true);
        survey.addQuestion(QuestionUtils.getMultiSelectQuestion("Multi Select Ques", "Choice4", "Choice5", "Choice6"), true);
        QuestionGroupDto questionGroupDto = mapper.map(survey);
        assertThat(questionGroupDto, is(notNullValue()));
        assertThat(questionGroupDto.getTitle(), is("Sur1"));
        assertEventSource(questionGroupDto.getEventSourceDto());
        List<SectionDto> sections = questionGroupDto.getSections();
        assertThat(sections, is(notNullValue()));
        assertThat(sections.size(), is(1));
        assertSection(sections.get(0));
    }

    @Test
    public void shouldMapToQuestionGroupInstanceDto() throws ApplicationException {
        Survey survey = getSurvey("Sur1", "Ques1");
        SurveyInstance surveyInstance = getSurveyInstance(survey, 12, 101, "Answer1");
        Integer questionGroupId = 11, sectionQuestionId = 112233;
        Integer questionId = survey.getQuestions().get(0).getQuestion().getQuestionId();
        when(sectionQuestionDao.retrieveIdFromQuestionGroupIdQuestionIdSectionName("Misc", questionId, questionGroupId)).thenReturn(asList(sectionQuestionId));
        QuestionGroupInstanceDto questionGroupInstanceDto = mapper.map(surveyInstance, questionGroupId);
        assertThat(questionGroupInstanceDto, is(notNullValue()));
        assertThat(questionGroupInstanceDto.getCreatorId(), is(12));
        assertThat(questionGroupInstanceDto.getEntityId(), is(101));
        assertThat(questionGroupInstanceDto.getQuestionGroupId(), is(questionGroupId));
        assertThat(questionGroupInstanceDto.getDateConducted(), is(surveyInstance.getDateConducted()));
        List<QuestionGroupResponseDto> questionGroupResponses = questionGroupInstanceDto.getQuestionGroupResponseDtos();
        assertThat(questionGroupResponses, is(notNullValue()));
        assertThat(questionGroupResponses.size(), is(1));
        assertThat(questionGroupResponses.get(0).getResponse(), is("Answer1"));
        assertThat(questionGroupResponses.get(0).getSectionQuestionId(), is(sectionQuestionId));
        verify(sectionQuestionDao, times(1)).retrieveIdFromQuestionGroupIdQuestionIdSectionName("Misc", questionId, questionGroupId);
    }

    @Test
    public void shouldMapToQuestionGroupInstanceDtoForMultiSelect() throws ApplicationException {
        Survey survey = getSurvey("Sur1", "Ques1");
        survey.getQuestions().get(0).getQuestion().setAnswerType(AnswerType.MULTISELECT);
        SurveyInstance surveyInstance = getSurveyInstance(survey, 12, 101, ",Answer1,Answer2,,Answer3");
        Integer questionGroupId = 11, sectionQuestionId = 112233;
        Integer questionId = survey.getQuestions().get(0).getQuestion().getQuestionId();
        when(sectionQuestionDao.retrieveIdFromQuestionGroupIdQuestionIdSectionName("Misc", questionId, questionGroupId)).thenReturn(asList(sectionQuestionId));
        QuestionGroupInstanceDto questionGroupInstanceDto = mapper.map(surveyInstance, questionGroupId);
        assertThat(questionGroupInstanceDto, is(notNullValue()));
        assertThat(questionGroupInstanceDto.getCreatorId(), is(12));
        assertThat(questionGroupInstanceDto.getEntityId(), is(101));
        assertThat(questionGroupInstanceDto.getQuestionGroupId(), is(questionGroupId));
        assertThat(questionGroupInstanceDto.getDateConducted(), is(surveyInstance.getDateConducted()));
        List<QuestionGroupResponseDto> questionGroupResponses = questionGroupInstanceDto.getQuestionGroupResponseDtos();
        assertThat(questionGroupResponses, is(notNullValue()));
        assertThat(questionGroupResponses.size(), is(3));
        assertThat(questionGroupResponses.get(0).getResponse(), is("Answer1"));
        assertThat(questionGroupResponses.get(0).getSectionQuestionId(), is(sectionQuestionId));
        assertThat(questionGroupResponses.get(1).getResponse(), is("Answer2"));
        assertThat(questionGroupResponses.get(1).getSectionQuestionId(), is(sectionQuestionId));
        assertThat(questionGroupResponses.get(2).getResponse(), is("Answer3"));
        assertThat(questionGroupResponses.get(2).getSectionQuestionId(), is(sectionQuestionId));
        verify(sectionQuestionDao, times(1)).retrieveIdFromQuestionGroupIdQuestionIdSectionName("Misc", questionId, questionGroupId);
    }

    private void assertSection(SectionDto sectionDto) {
        assertThat(sectionDto.getName(), is("Misc"));
        assertThat(sectionDto.getOrder(), is(0));
        List<QuestionDto> questions = sectionDto.getQuestions();
        assertThat(questions, is(notNullValue()));
        assertThat(questions.size(), is(5));
        assertFreeTextQuestion(questions.get(0), 0);
        assertNumericQuestion(questions.get(1), 1);
        assertDateQuestion(questions.get(2), 2);
        assertSingleSelectQuestion(questions.get(3), 3);
        assertMultiSelectQuestion(questions.get(4), 4);
    }

    private void assertSingleSelectQuestion(QuestionDto questionDto, int order) {
        assertThat(questionDto.getTitle(), is("Single Select Ques"));
        assertThat(questionDto.getType(), is(QuestionType.SINGLE_SELECT));
        assertThat(questionDto.getOrder(), is(order));
        List<ChoiceDto> choiceDtos = questionDto.getChoices();
        assertThat(choiceDtos, is(notNullValue()));
        assertThat(choiceDtos.size(), is(3));
        assertThat(choiceDtos.get(0).getValue(), is("Choice1"));
        assertThat(choiceDtos.get(0).getOrder(), is(0));
        assertThat(choiceDtos.get(1).getValue(), is("Choice2"));
        assertThat(choiceDtos.get(1).getOrder(), is(1));
        assertThat(choiceDtos.get(2).getValue(), is("Choice3"));
        assertThat(choiceDtos.get(2).getOrder(), is(2));
    }

    private void assertMultiSelectQuestion(QuestionDto questionDto, int order) {
        assertThat(questionDto.getTitle(), is("Multi Select Ques"));
        assertThat(questionDto.getType(), is(QuestionType.MULTI_SELECT));
        assertThat(questionDto.getOrder(), is(order));
        List<ChoiceDto> choiceDtos = questionDto.getChoices();
        assertThat(choiceDtos, is(notNullValue()));
        assertThat(choiceDtos.size(), is(3));
        assertThat(choiceDtos.get(0).getValue(), is("Choice4"));
        assertThat(choiceDtos.get(0).getOrder(), is(0));
        assertThat(choiceDtos.get(1).getValue(), is("Choice5"));
        assertThat(choiceDtos.get(1).getOrder(), is(1));
        assertThat(choiceDtos.get(2).getValue(), is("Choice6"));
        assertThat(choiceDtos.get(2).getOrder(), is(2));
    }

    private void assertDateQuestion(QuestionDto questionDto, int order) {
        assertThat(questionDto.getTitle(), is("Date Ques"));
        assertThat(questionDto.getType(), is(QuestionType.DATE));
        assertThat(questionDto.getOrder(), is(order));
    }

    private void assertNumericQuestion(QuestionDto questionDto, int order) {
        assertThat(questionDto.getTitle(), is("Numeric Ques"));
        assertThat(questionDto.getType(), is(QuestionType.NUMERIC));
        assertThat(questionDto.getOrder(), is(order));
        assertThat(questionDto.getMinValue(), is(30));
        assertThat(questionDto.getMaxValue(), is(300));
    }

    private void assertFreeTextQuestion(QuestionDto questionDto, int order) {
        assertThat(questionDto.getTitle(), is("FreeText Ques"));
        assertThat(questionDto.getType(), is(QuestionType.FREETEXT));
        assertThat(questionDto.getOrder(), is(order));
    }

    private void assertEventSource(EventSourceDto eventSourceDto) {
        assertThat(eventSourceDto, is(notNullValue()));
        assertThat(eventSourceDto.getEvent(), is("View"));
        assertThat(eventSourceDto.getSource(), is("Client"));
    }

    private void assertQuestion(QuestionDto questionDto, String title, QuestionType type, int order) {
        assertThat(questionDto.getTitle(), is(title));
        assertThat(questionDto.getType(), is(type));
        assertThat(questionDto.getOrder(), is(order));
    }
}