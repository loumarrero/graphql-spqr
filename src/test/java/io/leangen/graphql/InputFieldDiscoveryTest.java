package io.leangen.graphql;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.leangen.geantyref.GenericTypeReflector;
import io.leangen.graphql.annotations.GraphQLIgnore;
import io.leangen.graphql.annotations.GraphQLInputField;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.metadata.InputField;
import io.leangen.graphql.metadata.strategy.DefaultInclusionStrategy;
import io.leangen.graphql.metadata.strategy.InclusionStrategy;
import io.leangen.graphql.metadata.strategy.type.DefaultTypeTransformer;
import io.leangen.graphql.metadata.strategy.type.TypeTransformer;
import io.leangen.graphql.metadata.strategy.value.InputFieldDiscoveryStrategy;
import io.leangen.graphql.metadata.strategy.value.gson.GsonValueMapper;
import io.leangen.graphql.metadata.strategy.value.gson.GsonValueMapperFactory;
import io.leangen.graphql.metadata.strategy.value.jackson.JacksonValueMapper;
import io.leangen.graphql.metadata.strategy.value.jackson.JacksonValueMapperFactory;
import org.junit.Test;

import java.lang.reflect.AnnotatedType;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class InputFieldDiscoveryTest {

    private JacksonValueMapper jackson = new JacksonValueMapperFactory().getValueMapper();
    private GsonValueMapper gson = new GsonValueMapperFactory().getValueMapper();

    private static final InclusionStrategy INCLUSION_STRATEGY = new DefaultInclusionStrategy("io.leangen");
    private static final TypeTransformer TYPE_TRANSFORMER = new DefaultTypeTransformer(false, false);
    private static final AnnotatedType IGNORED_TYPE = GenericTypeReflector.annotate(Object.class);

    private static final InputField[] expectedDefaultFields = new InputField[] {
            new InputField("field1", null, IGNORED_TYPE, null, null),
            new InputField("field2", null, IGNORED_TYPE, null, null),
            new InputField("field3", null, IGNORED_TYPE, null, null)
    };
    private static final InputField[] expectedFilteredDefaultFields = new InputField[] {expectedDefaultFields[0], expectedDefaultFields[2]};
    private static final InputField[] expectedExplicitFields = new InputField[] {
            new InputField("aaa", "AAA", IGNORED_TYPE, null, "AAAA"),
            new InputField("bbb", "BBB", IGNORED_TYPE, null, 2222),
            new InputField("ccc", "CCC", IGNORED_TYPE, null, 3333)
    };
    private static final InputField[] expectedQueryFields = new InputField[] {
            new InputField("aaa", null, IGNORED_TYPE, null, null),
            new InputField("bbb", null, IGNORED_TYPE, null, null),
            new InputField("ccc", null, IGNORED_TYPE, null, null)
    };
    
    @Test
    public void basicFieldsTest() {
        assertFieldNamesEqual(FieldsOnly.class, expectedDefaultFields);
    }

    @Test
    public void basicGettersTest() {
        assertFieldNamesEqual(GettersOnly.class, expectedDefaultFields);
    }

    @Test
    public void basicSettersTest() {
        assertFieldNamesEqual(SettersOnly.class, expectedDefaultFields);
    }

    @Test
    public void explicitFieldsTest() {
        assertFieldNamesEqual(ExplicitFields.class, expectedExplicitFields);
    }

    @Test
    public void explicitGettersTest() {
        assertFieldNamesEqual(ExplicitGetters.class, expectedExplicitFields);
    }

    @Test
    public void explicitSettersTest() {
        assertFieldNamesEqual(ExplicitSetters.class, expectedExplicitFields);
    }
    
    @Test
    public void queryFieldsTest() {
        assertFieldNamesEqual(QueryFields.class, expectedQueryFields);
    }

    @Test
    public void queryGettersTest() {
        assertFieldNamesEqual(QueryGetters.class, expectedQueryFields);
    }

    @Test
    public void querySettersTest() {
        assertFieldNamesEqual(QuerySetters.class, expectedQueryFields);
    }

    @Test
    public void mixedFieldsTest() {
        assertFieldNamesEqual(MixedFieldsWin.class, expectedExplicitFields);
    }
    
    @Test
    public void mixedGettersTest() {
        assertFieldNamesEqual(MixedGettersWin.class, expectedExplicitFields);
    }

    @Test
    public void mixedSettersTest() {
        assertFieldNamesEqual(MixedSettersWin.class, expectedExplicitFields);
    }

    @Test
    public void conflictingGettersTest() {
        assertFieldNamesEqual(ConflictingGettersWin.class, expectedExplicitFields);
    }

    @Test
    public void conflictingSettersTest() {
        assertFieldNamesEqual(ConflictingSettersWin.class, expectedExplicitFields);
    }

    @Test
    public void allConflictingSettersTest() {
        assertFieldNamesEqual(AllConflictingSettersWin.class, expectedExplicitFields);
    }

    @Test
    public void hiddenSettersTest() {
        assertFieldNamesEqual(HiddenSetters.class, expectedFilteredDefaultFields);
    }

    @Test
    public void hiddenCtorParamsTest() {
        assertFieldNamesEqual(jackson, HiddenCtorParams.class, expectedFilteredDefaultFields);
    }

    private void assertFieldNamesEqual(Class typeToScan, InputField... expectedFields) {
        Set<InputField> jFields = assertFieldNamesEqual(jackson, typeToScan, expectedFields);
        Set<InputField> gFields = assertFieldNamesEqual(gson, typeToScan, expectedFields);

        assertAllFieldsEqual(jFields, gFields);
    }

    private Set<InputField> assertFieldNamesEqual(InputFieldDiscoveryStrategy mapper, Class typeToScan, InputField[] templates) {
        Set<InputField> fields = mapper.getInputFields(GenericTypeReflector.annotate(typeToScan), INCLUSION_STRATEGY, TYPE_TRANSFORMER);
        assertEquals(templates.length, fields.size());
        for (InputField template : templates) {
            Optional<InputField> field = fields.stream().filter(input -> input.getName().equals(template.getName())).findFirst();
            assertTrue("Field '" + template.getName() + "' doesn't match between different strategies", field.isPresent());
            assertEquals(template.getDescription(), field.get().getDescription());
            assertEquals(template.getDefaultValue(), field.get().getDefaultValue());
        }
        return fields;
    }

    private void assertAllFieldsEqual(Set<InputField> fields1, Set<InputField> fields2) {
        assertEquals(fields1.size(), fields2.size());
        fields1.forEach(f1 -> assertTrue(fields2.stream().anyMatch(f2 -> f1.getName().equals(f2.getName())
                        && Objects.equals(f1.getDescription(), f2.getDescription())
                        && GenericTypeReflector.equals(f1.getJavaType(), f2.getJavaType())
                        && Objects.equals(f1.getDefaultValue(), f2.getDefaultValue()))));
    }

    private class FieldsOnly {
        public String field1;
        public int field2;
        public Object field3;
    }

    private class GettersOnly {
        private String field1;
        private int field2;
        private Object field3;

        public String getField1() {
            return field1;
        }

        public int getField2() {
            return field2;
        }

        public Object getField3() {
            return field3;
        }
    }

    private class SettersOnly {
        private String field1;
        private int field2;
        private Object field3;

        public void setField1(String field1) {
            this.field1 = field1;
        }

        public void setField2(int field2) {
            this.field2 = field2;
        }

        public void setField3(Object field3) {
            this.field3 = field3;
        }
    }

    private class ExplicitFields {
        @GraphQLInputField(name = "aaa", description = "AAA", defaultValue = "AAAA")
        public String field1;
        @GraphQLInputField(name = "bbb", description = "BBB", defaultValue = "2222")
        public int field2;
        @GraphQLInputField(name = "ccc", description = "CCC", defaultValue = "3333")
        public Object field3;
    }

    private class ExplicitGetters {
        private String field1;
        private int field2;
        private Object field3;

        @GraphQLInputField(name = "aaa", description = "AAA", defaultValue = "AAAA")
        public String getField1() {
            return field1;
        }

        @GraphQLInputField(name = "bbb", description = "BBB", defaultValue = "2222")
        public int getField2() {
            return field2;
        }

        @GraphQLInputField(name = "ccc", description = "CCC", defaultValue = "3333")
        public Object getField3() {
            return field3;
        }
    }

    private class ExplicitSetters {
        private String field1;
        private int field2;
        private Object field3;

        @GraphQLInputField(name = "aaa", description = "AAA", defaultValue = "AAAA")
        public void setField1(String field1) {
            this.field1 = field1;
        }

        @GraphQLInputField(name = "bbb", description = "BBB", defaultValue = "2222")
        public void setField2(int field2) {
            this.field2 = field2;
        }

        @GraphQLInputField(name = "ccc", description = "CCC", defaultValue = "3333")
        public void setField3(Object field3) {
            this.field3 = field3;
        }
    }
    
    private class QueryFields {
        @GraphQLQuery(name = "aaa")
        public String field1;
        @GraphQLQuery(name = "bbb")
        public int field2;
        @GraphQLQuery(name = "ccc")
        public Object field3;
    }

    private class QueryGetters {
        private String field1;
        private int field2;
        private Object field3;

        @GraphQLQuery(name = "aaa")
        public String getField1() {
            return field1;
        }

        @GraphQLQuery(name = "bbb")
        public int getField2() {
            return field2;
        }

        @GraphQLQuery(name = "ccc")
        public Object getField3() {
            return field3;
        }
    }

    private class QuerySetters {
        private String field1;
        private int field2;
        private Object field3;

        @GraphQLQuery(name = "aaa")
        public void setField1(String field1) {
            this.field1 = field1;
        }

        @GraphQLQuery(name = "bbb")
        public void setField2(int field2) {
            this.field2 = field2;
        }

        @GraphQLQuery(name = "ccc")
        public void setField3(Object field3) {
            this.field3 = field3;
        }
    }

    private class MixedFieldsWin {
        @GraphQLInputField(name = "aaa", description = "AAA", defaultValue = "AAAA")
        private String field1;
        @GraphQLInputField(name = "bbb", description = "BBB", defaultValue = "2222")
        private int field2;
        @GraphQLInputField(name = "ccc", description = "CCC", defaultValue = "3333")
        private Object field3;

        @GraphQLQuery(name = "xxx")
        public String getField1() {
            return field1;
        }

        @GraphQLQuery(name = "yyy")
        public int getField2() {
            return field2;
        }

        @GraphQLQuery(name = "zzz")
        public Object getField3() {
            return field3;
        }
    }
    
    private class MixedGettersWin {
        @GraphQLQuery(name = "xxx")
        private String field1;
        @GraphQLQuery(name = "yyy")
        private int field2;
        @GraphQLQuery(name = "zzz")
        private Object field3;

        @GraphQLInputField(name = "aaa", description = "AAA", defaultValue = "AAAA")
        public String getField1() {
            return field1;
        }

        @GraphQLInputField(name = "bbb", description = "BBB", defaultValue = "2222")
        public int getField2() {
            return field2;
        }

        @GraphQLInputField(name = "ccc", description = "CCC", defaultValue = "3333")
        public Object getField3() {
            return field3;
        }
    }

    private class MixedSettersWin {
        @GraphQLQuery(name = "xxx")
        private String field1;
        @GraphQLQuery(name = "yyy")
        private int field2;
        @GraphQLQuery(name = "zzz")
        private Object field3;

        @GraphQLInputField(name = "aaa", description = "AAA", defaultValue = "AAAA")
        public void setField1(String field1) {
            this.field1 = field1;
        }

        @GraphQLInputField(name = "bbb", description = "BBB", defaultValue = "2222")
        public void setField2(int field2) {
            this.field2 = field2;
        }

        @GraphQLInputField(name = "ccc", description = "CCC", defaultValue = "3333")
        public void setField3(Object field3) {
            this.field3 = field3;
        }
    }

    private class ConflictingGettersWin {
        @GraphQLInputField(name = "xxx", description = "XXX", defaultValue = "XXXX")
        private String field1;
        @GraphQLInputField(name = "yyy", description = "YYY", defaultValue = "-1")
        private int field2;
        @GraphQLInputField(name = "zzz", description = "ZZZ", defaultValue = "-1")
        private Object field3;

        @GraphQLInputField(name = "aaa", description = "AAA", defaultValue = "AAAA")
        public String getField1() {
            return field1;
        }

        @GraphQLInputField(name = "bbb", description = "BBB", defaultValue = "2222")
        public int getField2() {
            return field2;
        }

        @GraphQLInputField(name = "ccc", description = "CCC", defaultValue = "3333")
        public Object getField3() {
            return field3;
        }
    }
    
    private class ConflictingSettersWin {
        @GraphQLInputField(name = "xxx", description = "XXX", defaultValue = "XXXX")
        private String field1;
        @GraphQLInputField(name = "yyy", description = "YYY", defaultValue = "-1")
        private int field2;
        @GraphQLInputField(name = "zzz", description = "ZZZ", defaultValue = "-1")
        private Object field3;

        @GraphQLInputField(name = "aaa", description = "AAA", defaultValue = "AAAA")
        public void setField1(String field1) {
            this.field1 = field1;
        }

        @GraphQLInputField(name = "bbb", description = "BBB", defaultValue = "2222")
        public void setField2(int field2) {
            this.field2 = field2;
        }

        @GraphQLInputField(name = "ccc", description = "CCC", defaultValue = "3333")
        public void setField3(Object field3) {
            this.field3 = field3;
        }
    }

    private class AllConflictingSettersWin {
        @GraphQLInputField(name = "xxx", description = "XXX", defaultValue = "XXXX")
        private String field1;
        @GraphQLInputField(name = "yyy", description = "YYY", defaultValue = "-1")
        private int field2;
        @GraphQLInputField(name = "zzz", description = "ZZZ", defaultValue = "-1")
        private Object field3;

        @GraphQLInputField(name = "111", description = "1111", defaultValue = "XXXX")
        public String getField1() {
            return field1;
        }

        @GraphQLInputField(name = "222", description = "2222", defaultValue = "-1")
        public int getField2() {
            return field2;
        }

        @GraphQLInputField(name = "333", description = "3333", defaultValue = "-1")
        public Object getField3() {
            return field3;
        }

        @GraphQLInputField(name = "aaa", description = "AAA", defaultValue = "AAAA")
        public void setField1(String field1) {
            this.field1 = field1;
        }

        @GraphQLInputField(name = "bbb", description = "BBB", defaultValue = "2222")
        public void setField2(int field2) {
            this.field2 = field2;
        }

        @GraphQLInputField(name = "ccc", description = "CCC", defaultValue = "3333")
        public void setField3(Object field3) {
            this.field3 = field3;
        }
    }

    private class HiddenSetters {
        private String field1;
        private int field2;
        private Object field3;

        public String getField1() {
            return field1;
        }

        public void setField1(String field1) {
            this.field1 = field1;
        }

        @GraphQLQuery(name = "ignored")
        @GraphQLInputField(name = "ignored")
        public int getField2() {
            return field2;
        }

        @GraphQLIgnore
        @GraphQLInputField(name = "ignored")
        public void setField2(int field2) {
            this.field2 = field2;
        }

        public Object getField3() {
            return field3;
        }

        @GraphQLInputField
        public void setField3(Object field3) {
            this.field3 = field3;
        }
    }

    public static class HiddenCtorParams {
        private String field1;
        private int field2;
        private Object field3;

        @JsonCreator
        public HiddenCtorParams(String field1, @GraphQLIgnore int field2, Object field3) {
            this.field1 = field1;
            this.field2 = field2;
            this.field3 = field3;
        }
    }
}
