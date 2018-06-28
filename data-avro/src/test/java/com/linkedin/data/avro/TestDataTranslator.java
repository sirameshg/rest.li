/*
   Copyright (c) 2012 LinkedIn Corp.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

package com.linkedin.data.avro;

import com.google.common.collect.ImmutableList;
import com.linkedin.data.DataMap;
import com.linkedin.data.TestUtil;
import com.linkedin.data.avro.util.AvroUtil;
import com.linkedin.data.schema.RecordDataSchema;
import com.linkedin.data.schema.validation.CoercionMode;
import com.linkedin.data.schema.validation.RequiredMode;
import com.linkedin.data.schema.validation.ValidateDataAgainstSchema;
import com.linkedin.data.schema.validation.ValidationOptions;
import com.linkedin.data.schema.validation.ValidationResult;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericArray;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class TestDataTranslator
{
  public static final PrintStream out = new PrintStream(new FileOutputStream(FileDescriptor.out));

  public static final String ONE_WAY = "ONE_WAY";

  @Test
  public void testDataTranslator() throws IOException
  {
    boolean debug = false;

    String[][][] inputs =
    {
      // {
      //   {
      //     1 string holding the Pegasus schema in JSON.
      //       The string may be marked with ##T_START and ##T_END markers. The markers are used for typeref testing.
      //       If the string these markers, then two schemas will be constructed and tested.
      //       The first schema replaces these markers with two empty strings.
      //       The second schema replaces these markers with a typeref enclosing the type between these markers.
      //   },
      //   {
      //     1st string is input DataMap, JSON will be deserialized into DataMap.
      //     2nd string is expected output after translating from DataMap to Avro GenericRecord
      //       if translation is successful, this string should be Avro GenericRecord serialized to JSON,
      //       else the output should be a string providing diagnostic messages regarding the translation
      //       failure. In this case, the 2nd string provides a string that will be checked against the
      //       diagnostic messages. The diagnostic message should contain this string.
      //   }
      // }
      {
        // record with int field
        {
          "{\n" +
          "  \"type\" : \"record\",\n" +
          "  \"name\" : \"Foo\",\n" +
          "  \"fields\" : [\n" +
          "    { \"name\" : \"intRequired\", \"type\" : ##T_START \"int\" ##T_END }\n" +
          "  ]\n" +
          "}\n"
        },
        {
          "{ \"intRequired\" : 42 }",
          "{\"intRequired\":42}"
        },
        {
          "{ }",
          "Error processing /intRequired"
        },
        {
          "{ \"intRequired\" : null }",
          "Error processing /intRequired"
        },
        {
          "{ \"intRequired\" : \"string\" }",
          "Error processing /intRequired"
        }
      },
      // record with long field
      {
        {
          "{\n" +
          "  \"type\" : \"record\",\n" +
          "  \"name\" : \"Foo\",\n" +
          "  \"fields\" : [\n" +
          "    { \"name\" : \"longRequired\", \"type\" : ##T_START \"long\" ##T_END }\n" +
          "  ]\n" +
          "}\n"
        },
        {
          "{ \"longRequired\" : 42 }",
          "{\"longRequired\":42}"
        },
        {
          "{ }",
          "Error processing /longRequired"
        },
        {
          "{ \"longRequired\" : null }",
          "Error processing /longRequired"
        },
        {
          "{ \"longRequired\" : \"string\" }",
          "Error processing /longRequired"
        }
      },
      // record with float field
      {
        {
          "{\n" +
          "  \"type\" : \"record\",\n" +
          "  \"name\" : \"Foo\",\n" +
          "  \"fields\" : [\n" +
          "    { \"name\" : \"floatRequired\", \"type\" : ##T_START \"float\" ##T_END }\n" +
          "  ]\n" +
          "}\n"
        },
        {
          "{ \"floatRequired\" : 42 }",
          "{\"floatRequired\":42.0}"
        },
        {
          "{ }",
          "Error processing /floatRequired"
        },
        {
          "{ \"floatRequired\" : null }",
          "Error processing /floatRequired"
        },
        {
          "{ \"floatRequired\" : \"string\" }",
          "Error processing /floatRequired"
        }
      },
      // record with double field
      {
        {
          "{\n" +
          "  \"type\" : \"record\",\n" +
          "  \"name\" : \"Foo\",\n" +
          "  \"fields\" : [\n" +
          "    { \"name\" : \"doubleRequired\", \"type\" : ##T_START \"double\" ##T_END }\n" +
          "  ]\n" +
          "}\n"
        },
        {
          "{ \"doubleRequired\" : 42 }",
          "{\"doubleRequired\":42.0}"
        },
        {
          "{ }",
          "Error processing /doubleRequired"
        },
        {
          "{ \"doubleRequired\" : null }",
          "Error processing /doubleRequired"
        },
        {
          "{ \"doubleRequired\" : \"string\" }",
          "Error processing /doubleRequired"
        }
      },
      {
        // record with boolean field
        {
          "{\n" +
          "  \"type\" : \"record\",\n" +
          "  \"name\" : \"Foo\",\n" +
          "  \"fields\" : [\n" +
          "    { \"name\" : \"booleanRequired\", \"type\" : ##T_START \"boolean\" ##T_END }\n" +
          "  ]\n" +
          "}\n"
        },
        {
          "{ \"booleanRequired\" : true }",
          "{\"booleanRequired\":true}"
        },
        {
          "{ \"booleanRequired\" : false }",
          "{\"booleanRequired\":false}"
        },
        {
          "{ }",
          "Error processing /booleanRequired"
        },
        {
          "{ \"booleanRequired\" : null }",
          "Error processing /booleanRequired"
        },
        {
          "{ \"booleanRequired\" : \"string\" }",
          "Error processing /booleanRequired"
        }
      },
      {
        // record with string field
        {
          "{\n" +
          "  \"type\" : \"record\",\n" +
          "  \"name\" : \"Foo\",\n" +
          "  \"fields\" : [\n" +
          "    { \"name\" : \"stringRequired\", \"type\" : ##T_START \"string\" ##T_END }\n" +
          "  ]\n" +
          "}\n"
        },
        {
          "{ \"stringRequired\" : \"bar\" }",
          "{\"stringRequired\":\"bar\"}"
        },
        {
          "{ }",
          "Error processing /stringRequired"
        },
        {
          "{ \"stringRequired\" : null }",
          "Error processing /stringRequired"
        },
        {
          "{ \"stringRequired\" : false }",
          "Error processing /stringRequired"
        }
      },
      {
        // record with bytes field
        {
          "{\n" +
          "  \"type\" : \"record\",\n" +
          "  \"name\" : \"Foo\",\n" +
          "  \"fields\" : [\n" +
          "    { \"name\" : \"bytesRequired\", \"type\" : ##T_START \"bytes\" ##T_END }\n" +
          "  ]\n" +
          "}\n"
        },
        {
          "{ \"bytesRequired\" : \"12345\\u0001\" }",
          "{\"bytesRequired\":\"12345\\u0001\"}"
        },
        {
          "{ }",
          "Error processing /bytesRequired"
        },
        {
          "{ \"bytesRequired\" : null }",
          "Error processing /bytesRequired"
        },
        {
          "{ \"bytesRequired\" : false }",
          "Error processing /bytesRequired"
        },
        {
          "{ \"bytesRequired\" : \"1234\\u0101\" }",
          "Error processing /bytesRequired"
        }
      },
      {
        // record with fixed field
        {
          "{\n" +
          "  \"type\" : \"record\",\n" +
          "  \"name\" : \"Foo\",\n" +
          "  \"fields\" : [\n" +
          "    {\n" +
          "      \"name\" : \"fixedRequired\",\n" +
          "      \"type\" : ##T_START { \"type\" : \"fixed\", \"name\" : \"Fixed5\", \"size\" : 5 } ##T_END\n" +
          "    }\n" +
          "  ]\n" +
          "}\n"
        },
        {
          "{ \"fixedRequired\" : \"12345\" }",
          "{\"fixedRequired\":\"12345\"}"
        },
        {
          "{ }",
          "Error processing /fixedRequired"
        },
        {
          "{ \"fixedRequired\" : null }",
          "Error processing /fixedRequired"
        },
        {
          "{ \"fixedRequired\" : false }",
          "Error processing /fixedRequired"
        },
        {
          "{ \"fixedRequired\" : \"1234\" }",
          "Error processing /fixedRequired"
        },
        {
          "{ \"fixedRequired\" : \"123456\" }",
          "Error processing /fixedRequired"
        }
      },
      {
        // record with enum field
        {
          "{\n" +
          "  \"type\" : \"record\",\n" +
          "  \"name\" : \"Foo\",\n" +
          "  \"fields\" : [\n" +
          "    {\n" +
          "      \"name\" : \"enumRequired\",\n" +
          "      \"type\" : ##T_START {\n" +
          "        \"name\" : \"Fruits\",\n" +
          "        \"type\" : \"enum\",\n" +
          "        \"symbols\" : [ \"APPLE\", \"ORANGE\" ]\n" +
          "      } ##T_END\n" +
          "    }\n" +
          "  ]\n" +
          "}\n"
        },
        {
          "{ \"enumRequired\" : \"APPLE\" }",
          "{\"enumRequired\":\"APPLE\"}"
        },
        {
          "{ \"enumRequired\" : \"ORANGE\" }",
          "{\"enumRequired\":\"ORANGE\"}"
        },
        {
          "{ }",
          "Error processing /enumRequired"
        },
        {
          "{ \"enumRequired\" : null }",
          "Error processing /enumRequired"
        },
        {
          "{ \"enumRequired\" : false }",
          "Error processing /enumRequired"
        }
      },
      {
        // record with array field
        {
          "{\n" +
          "  \"type\" : \"record\",\n" +
          "  \"name\" : \"Foo\",\n" +
          "  \"fields\" : [\n" +
          "    {\n" +
          "      \"name\" : \"arrayRequired\",\n" +
          "      \"type\" : ##T_START {\n" +
          "        \"type\" : \"array\",\n" +
          "        \"items\" : {\n" +
          "          \"name\" : \"Fruits\",\n" +
          "          \"type\" : \"enum\",\n" +
          "          \"symbols\" : [ \"APPLE\", \"ORANGE\" ]\n" +
          "        }\n" +
          "      } ##T_END\n" +
          "    }\n" +
          "  ]\n" +
          "}\n"
        },
        {
          "{ \"arrayRequired\" : [] }",
          "{\"arrayRequired\":[]}"
        },
        {
          "{ \"arrayRequired\" : [ \"APPLE\", \"ORANGE\" ] }",
          "{\"arrayRequired\":[\"APPLE\",\"ORANGE\"]}"
        },
        {
          "{ }",
          "Error processing /arrayRequired"
        },
        {
          "{ \"arrayRequired\" : null }",
          "Error processing /arrayRequired"
        },
        {
          "{ \"arrayRequired\" : {} }",
          "Error processing /arrayRequired"
        },
        {
          "{ \"arrayRequired\" : [ null ] }",
          "Error processing /arrayRequired/0"
        },
        {
          "{ \"arrayRequired\" : [ \"APPLE\", \"PINEAPPLE\" ] }",
          "Error processing /arrayRequired/1"
        }
      },
      {
        // record with map field
        {
          "{\n" +
          "  \"type\" : \"record\",\n" +
          "  \"name\" : \"Foo\",\n" +
          "  \"fields\" : [\n" +
          "    {\n" +
          "      \"name\" : \"mapRequired\",\n" +
          "      \"type\" : ##T_START {\n" +
          "        \"type\" : \"map\",\n" +
          "        \"values\" : \"int\" " +
          "      } ##T_END\n" +
          "    }\n" +
          "  ]\n" +
          "}\n"
        },
        {
          "{ \"mapRequired\" : {} }",
          "{\"mapRequired\":{}}"
        },
        {
          "{ \"mapRequired\" : { \"x\" : 1} }",
          "{\"mapRequired\":{\"x\":1}}"
        },
        {
          "{ }",
          "Error processing /mapRequired"
        },
        {
          "{ \"mapRequired\" : null }",
          "Error processing /mapRequired"
        },
        {
          "{ \"mapRequired\" : [] }",
          "Error processing /mapRequired"
        },
        {
          "{ \"mapRequired\" : { \"x\" : null } }",
          "Error processing /mapRequired/x"
        },
        {
          "{ \"mapRequired\" : { \"x\" : \"PINEAPPLE\" } }",
          "Error processing /mapRequired/x"
        }
      },
      {
        // record with union field
        {
          "{\n" +
          "  \"type\" : \"record\",\n" +
          "  \"name\" : \"foo.Foo\",\n" +
          "  \"fields\" : [\n" +
          "    {\n" +
          "      \"name\" : \"unionRequired\",\n" +
          "      \"type\" : ##T_START [ \"int\", \"string\", \"foo.Foo\" ] ##T_END\n" +
          "    }\n" +
          "  ]\n" +
          "}\n"
        },
        {
          "{ \"unionRequired\" : { \"int\" : 5 } }",
          "{\"unionRequired\":{\"int\":5}}"
        },
        {
          "{ \"unionRequired\" : { \"string\" : \"s1\" }  }",
          "{\"unionRequired\":{\"string\":\"s1\"}}"
        },
        {
          "{ \"unionRequired\" : { \"foo.Foo\" : { \"unionRequired\" : { \"int\" : 5 } } } }",
          "{\"unionRequired\":{\"##NS(foo.)Foo\":{\"unionRequired\":{\"int\":5}}}}"
        },
        {
          "{ }",
          "Error processing /unionRequired"
        },
        {
          "{ \"unionRequired\" : null }",
          "Error processing /unionRequired"
        },
        {
          "{ \"unionRequired\" : {} }",
          "Error processing /unionRequired"
        },
        {
          "{ \"unionRequired\" : { \"bad\" : 0 } }",
          "Error processing /unionRequired"
        }
      },
      {
        // record with a required "union with aliases" field
        {
          "{\n" +
          "  \"type\" : \"record\",\n" +
          "  \"name\" : \"foo.Foo\",\n" +
          "  \"fields\" : [\n" +
          "    {\n" +
          "      \"name\" : \"uwaRequiredNoNull\",\n" +
          "      \"type\" : ##T_START [\n" +
          "        { \"alias\": \"success\", \"type\": \"string\" },\n" +
          "        { \"alias\": \"failure\", \"type\": \"string\" }\n" +
          "      ] ##T_END\n" +
          "    }\n" +
          "  ]\n" +
          "}\n"
        },
        {
          "{ \"uwaRequiredNoNull\" : { \"success\" : \"Union with aliases!\" } }",
          "{\"uwaRequiredNoNull\":{\"success\":{\"string\":\"Union with aliases!\"},\"failure\":null,\"fieldDiscriminator\":\"success\"}}"
        },
        {
          "{ \"uwaRequiredNoNull\" : { \"failure\" : \"Union with aliases!\" } }",
          "{\"uwaRequiredNoNull\":{\"success\":null,\"failure\":{\"string\":\"Union with aliases!\"},\"fieldDiscriminator\":\"failure\"}}"
        },
        {
          "{ \"uwaRequiredNoNull\" : null }",
          "Error processing /uwaRequiredNoNull"
        },
        {
          "{}",
          "Error processing /uwaRequiredNoNull"
        },
        {
          "{ \"uwaRequiredNoNull\" : {} }",
          "Error processing /uwaRequiredNoNull"
        },
        {
          "{ \"uwaRequiredNoNull\" : \"Union with aliases!\" }",
          "Error processing /uwaRequiredNoNull"
        },
        {
          "{ \"uwaRequiredNoNull\" : { \"string\" : \"Union with aliases!\" } }",
          "Error processing /uwaRequiredNoNull"
        },
        {
          "{ \"uwaRequiredNoNull\" : { \"success\" : 123 } }",
          "Error processing /uwaRequiredNoNull/success"
        }
      },
      {
        // record with a required "union with aliases" field with null member
        {
          "{\n" +
          "  \"type\" : \"record\",\n" +
          "  \"name\" : \"foo.Foo\",\n" +
          "  \"fields\" : [\n" +
          "    {\n" +
          "      \"name\" : \"uwaRequiredWithNull\",\n" +
          "      \"type\" : ##T_START [\n" +
          "        \"null\",\n" +
          "        { \"alias\": \"success\", \"type\": \"string\" },\n" +
          "        { \"alias\": \"failure\", \"type\": \"string\" }\n" +
          "      ] ##T_END\n" +
          "    }\n" +
          "  ]\n" +
          "}\n"
        },
        {
          "{ \"uwaRequiredWithNull\" : { \"success\" : \"Union with aliases!\" } }",
          "{\"uwaRequiredWithNull\":{\"success\":{\"string\":\"Union with aliases!\"},\"failure\":null,\"fieldDiscriminator\":\"success\"}}"
        },
        {
          "{ \"uwaRequiredWithNull\" : { \"failure\" : \"Union with aliases!\" } }",
          "{\"uwaRequiredWithNull\":{\"success\":null,\"failure\":{\"string\":\"Union with aliases!\"},\"fieldDiscriminator\":\"failure\"}}"
        },
        {
          "{ \"uwaRequiredWithNull\" : null }",
          "{\"uwaRequiredWithNull\":{\"success\":null,\"failure\":null,\"fieldDiscriminator\":\"null\"}}"
        },
        {
          "{}",
          "Error processing /uwaRequiredWithNull"
        },
        {
          "{ \"uwaRequiredWithNull\" : {} }",
          "Error processing /uwaRequiredWithNull"
        },
        {
          "{ \"uwaRequiredWithNull\" : \"Union with aliases!\" }",
          "Error processing /uwaRequiredWithNull"
        },
        {
          "{ \"uwaRequiredWithNull\" : { \"string\" : \"Union with aliases!\" } }",
          "Error processing /uwaRequiredWithNull"
        },
        {
          "{ \"uwaRequiredWithNull\" : { \"success\" : 123 } }",
          "Error processing /uwaRequiredWithNull/success"
        }
      },
      {
        // record with array of union with null field
        // this is to check that translation of union with null that does not get converted to optional,
        // and that null union member translates correctly from Data to Avro and Avro to Data.
        {
          "{\n" +
          "  \"type\" : \"record\",\n" +
          "  \"name\" : \"foo.Foo\",\n" +
          "  \"fields\" : [\n" +
          "    {\n" +
          "      \"name\" : \"arrayOfUnionWitNull\",\n" +
          "      \"type\" : ##T_START { \"type\" : \"array\", \"items\" : [ \"int\", \"null\" ] } ##T_END\n" +
          "    }\n" +
          "  ]\n" +
          "}\n"
        },
        {
          "{ \"arrayOfUnionWitNull\" : [ { \"int\" : 5 } ] }",
          "{\"arrayOfUnionWitNull\":[{\"int\":5}]}"
        },
        {
          "{ \"arrayOfUnionWitNull\" : [ null ] }",
          "{\"arrayOfUnionWitNull\":[null]}"
        },
        {
          "{ }",
          "Error processing /arrayOfUnionWitNull"
        },
        {
          "{ \"arrayOfUnionWitNull\" : [ {} ] }",
          "Error processing /arrayOfUnionWitNull/0"
        },
        {
          "{ \"arrayOfUnionWitNull\" : [ { \"bad\" : 0 } ] }",
          "Error processing /arrayOfUnionWitNull/0"
        }
      },
      {
        // record with record field.
        {
          "{ \n" +
          "  \"type\" : \"record\",\n" +
          "  \"name\" : \"Foo\",\n" +
          "  \"fields\" : [\n" +
          "    {\n" +
          "      \"name\" : \"bar\",\n" +
          "      \"type\" : ##T_START {\n" +
          "        \"name\" : \"Bar\",\n" +
          "        \"type\" : \"record\",\n" +
          "        \"fields\" : [\n" +
          "          {\n" +
          "            \"name\" : \"baz\",\n" +
          "            \"type\" : \"int\"\n" +
          "          }\n" +
          "        ]\n" +
          "      } ##T_END\n" +
          "    }\n" +
          "  ]\n" +
          "}\n"
        },
        {
          "{ \"bar\" : { \"baz\" : 1 } }",
          "{\"bar\":{\"baz\":1}}"
        },
        {
          "{ \"bar\" : { \"baz\" : null } }",
          "Error processing /bar/baz"
        },
      },
      //
      // Optional
      //
      {
        // record with optional non-union field
        {
          "{\n" +
          "  \"type\" : \"record\",\n" +
          "  \"name\" : \"Foo\",\n" +
          "  \"fields\" : [\n" +
          "    {\n" +
          "      \"name\" : \"intOptional\",\n" +
          "      \"type\" : ##T_START \"int\" ##T_END,\n" +
          "      \"optional\" : true\n" +
          "    }\n" +
          "  ]\n" +
          "}\n"
        },
        {
          "{ }",
          "{\"intOptional\":null}"
        },
        {
          "{ \"intOptional\" : 42 }",
          "{\"intOptional\":{\"int\":42}}"
        },
        {
          "{ \"intOptional\" : null }",
          "Error processing /intOptional"
        },
        {
          "{ \"intOptional\" : \"s1\" }",
          "Error processing /intOptional"
        },
        {
          "{ \"intOptional\" : {} }",
          "Error processing /intOptional"
        },
      },
      {
        // record with optional union field that does not include null
        {
          "{\n" +
          "  \"type\" : \"record\",\n" +
          "  \"name\" : \"Foo\",\n" +
          "  \"fields\" : [\n" +
          "    {\n" +
          "      \"name\" : \"unionOptional\",\n" +
          "      \"type\" : ##T_START [ \"int\", \"string\" ] ##T_END,\n" +
          "      \"optional\" : true\n" +
          "    }\n" +
          "  ]\n" +
          "}\n"
        },
        {
          "{ }",
          "{\"unionOptional\":null}"
        },
        {
          "{ \"unionOptional\" : { \"int\" : 42 } }",
          "{\"unionOptional\":{\"int\":42}}"
        },
        {
          "{ \"unionOptional\" : { \"string\" : \"s1\" } }",
          "{\"unionOptional\":{\"string\":\"s1\"}}"
        },
        {
          "{ \"unionOptional\" : null }",
          "Error processing /unionOptional"
        },
        {
          "{ \"unionOptional\" : \"s1\" }",
          "Error processing /unionOptional"
        },
        {
          "{ \"unionOptional\" : {} }",
          "Error processing /unionOptional"
        },
      },
      {
        // record with optional union field that includes null
        {
          "{\n" +
          "  \"type\" : \"record\",\n" +
          "  \"name\" : \"Foo\",\n" +
          "  \"fields\" : [\n" +
          "    {\n" +
          "      \"name\" : \"unionOptional\",\n" +
          "      \"type\" : ##T_START [ \"null\", \"string\" ] ##T_END,\n" +
          "      \"optional\" : true\n" +
          "    }\n" +
          "  ]\n" +
          "}\n"
        },
        {
          "{ }",
          "{\"unionOptional\":null}"
        },
        {
          "{ \"unionOptional\" : { \"string\" : \"s1\" } }",
          "{\"unionOptional\":{\"string\":\"s1\"}}"
        },
        {
          "{ \"unionOptional\" : null }",
          // The round-trip result will drop the optional field.
          // A null in the union is translated to an absent field.
          ONE_WAY,
          "{\"unionOptional\":null}"
        },
        {
          "{ \"unionOptional\" : \"s1\" }",
          "Error processing /unionOptional"
        },
        {
          "{ \"unionOptional\" : {} }",
          "Error processing /unionOptional"
        },
      },
      {
        // record with an optional "union with aliases" field
        {
          "{\n" +
          "  \"type\" : \"record\",\n" +
          "  \"name\" : \"foo.Foo\",\n" +
          "  \"fields\" : [\n" +
          "    {\n" +
          "      \"name\" : \"uwaOptionalNoNull\",\n" +
          "      \"type\" : ##T_START [\n" +
          "        { \"alias\": \"success\", \"type\": \"string\" },\n" +
          "        { \"alias\": \"failure\", \"type\": \"string\" }\n" +
          "      ] ##T_END,\n" +
          "      \"optional\": true\n" +
          "    }\n" +
          "  ]\n" +
          "}\n"
        },
        {
          "{ \"uwaOptionalNoNull\" : { \"success\" : \"Union with aliases!\" } }",
          "{\"uwaOptionalNoNull\":{\"##NS(foo.)FooUwaOptionalNoNull\":{\"success\":{\"string\":\"Union with aliases!\"},\"failure\":null,\"fieldDiscriminator\":\"success\"}}}"
        },
        {
          "{}",
          "{\"uwaOptionalNoNull\":null}"
        },
        {
          "{ \"uwaOptionalNoNull\" : null }",
          "Error processing /uwaOptionalNoNull"
        },
        {
          "{ \"uwaOptionalNoNull\" : {} }",
          "Error processing /uwaOptionalNoNull"
        },
        {
          "{ \"uwaOptionalNoNull\" : \"Union with aliases!\" }",
          "Error processing /uwaOptionalNoNull"
        },
        {
          "{ \"uwaOptionalNoNull\" : { \"string\" : \"Union with aliases!\" } }",
          "Error processing /uwaOptionalNoNull"
        },
        {
          "{ \"uwaOptionalNoNull\" : { \"success\" : 123 } }",
          "Error processing /uwaOptionalNoNull/success"
        }
      },
      {
        // record with an optional "union with aliases" field with null member
        {
          "{\n" +
          "  \"type\" : \"record\",\n" +
          "  \"name\" : \"foo.Foo\",\n" +
          "  \"fields\" : [\n" +
          "    {\n" +
          "      \"name\" : \"uwaOptionalWithNull\",\n" +
          "      \"type\" : ##T_START [\n" +
          "        \"null\",\n" +
          "        { \"alias\": \"success\", \"type\": \"string\" },\n" +
          "        { \"alias\": \"failure\", \"type\": \"string\" }\n" +
          "      ] ##T_END,\n" +
          "      \"optional\": true\n" +
          "    }\n" +
          "  ]\n" +
          "}\n"
        },
        {
          "{ \"uwaOptionalWithNull\" : { \"success\" : \"Union with aliases!\" } }",
          "{\"uwaOptionalWithNull\":{\"##NS(foo.)FooUwaOptionalWithNull\":{\"success\":{\"string\":\"Union with aliases!\"},\"failure\":null,\"fieldDiscriminator\":\"success\"}}}"
        },
        {
          "{}",
          "{\"uwaOptionalWithNull\":null}"
        },
        {
          "{ \"uwaOptionalWithNull\" : null }",
          "{\"uwaOptionalWithNull\":{\"##NS(foo.)FooUwaOptionalWithNull\":{\"success\":null,\"failure\":null,\"fieldDiscriminator\":\"null\"}}}"
        },
        {
          "{ \"uwaOptionalWithNull\" : {} }",
          "Error processing /uwaOptionalWithNull"
        },
        {
          "{ \"uwaOptionalWithNull\" : \"Union with aliases!\" }",
          "Error processing /uwaOptionalWithNull"
        },
        {
          "{ \"uwaOptionalWithNull\" : { \"string\" : \"Union with aliases!\" } }",
          "Error processing /uwaOptionalWithNull"
        },
        {
          "{ \"uwaOptionalWithNull\" : { \"success\" : 123 } }",
          "Error processing /uwaOptionalWithNull/success"
        }
      },
      {
        // record with optional enum field
        {
          "{\n" +
          "  \"type\" : \"record\",\n" +
          "  \"name\" : \"Foo\",\n" +
          "  \"fields\" : [\n" +
          "    {\n" +
          "      \"name\" : \"enumOptional\",\n" +
          "      \"type\" : ##T_START { \"type\" : \"enum\", \"name\" : \"foo.bar\", \"symbols\" : [ \"A\", \"B\" ] } ##T_END,\n" +
          "      \"optional\" : true\n" +
          "    }\n" +
          "  ]\n" +
          "}\n"
        },
        {
          "{ }",
          "{\"enumOptional\":null}"
        },
        {
          "{ \"enumOptional\" : \"A\" } }",
          "{\"enumOptional\":{\"##NS(foo.)bar\":\"A\"}}"
        },
        {
          "{ \"enumOptional\" : \"B\" } }",
          "{\"enumOptional\":{\"##NS(foo.)bar\":\"B\"}}"
        },
        {
          "{ \"enumOptional\" : {} }",
          "Error processing /enumOptional"
        },
      },
      {
        // record with optional enum field
        {
          "{\n" +
          "  \"type\" : \"record\",\n" +
          "  \"name\" : \"Foo\",\n" +
          "  \"fields\" : [\n" +
          "    {\n" +
          "      \"name\" : \"enumOptional\",\n" +
          "      \"type\" : ##T_START { \"type\" : \"enum\", \"name\" : \"foo.bar\", \"symbols\" : [ \"A\", \"B\" ] } ##T_END,\n" +
          "      \"optional\" : true\n" +
          "    }\n" +
          "  ]\n" +
          "}\n"
        },
        {
          "{ }",
          "{\"enumOptional\":null}"
        },
        {
          "{ \"enumOptional\" : \"A\" } }",
          "{\"enumOptional\":{\"##NS(foo.)bar\":\"A\"}}"
        },
        {
          "{ \"enumOptional\" : \"B\" } }",
          "{\"enumOptional\":{\"##NS(foo.)bar\":\"B\"}}"
        },
        {
          "{ \"enumOptional\" : {} }",
          "Error processing /enumOptional"
        },
      },
      {
        // record with optional union field of records
        {
          "{\n" +
          "  \"type\" : \"record\",\n" +
          "  \"name\" : \"Foo\",\n" +
          "  \"fields\" : [\n" +
          "    {\n" +
          "      \"name\" : \"unionOptional\",\n" +
          "      \"type\" : ##T_START [\n" +
          "        { \"type\" : \"record\", \"name\" : \"R1\", \"fields\" : [ { \"name\" : \"r1\", \"type\" : \"string\" } ] },\n" +
          "        { \"type\" : \"record\", \"name\" : \"R2\", \"fields\" : [ { \"name\" : \"r2\", \"type\" : \"int\" } ] },\n" +
          "        \"int\",\n" +
          "        \"string\"\n" +
          "      ] ##T_END,\n" +
          "      \"optional\" : true\n" +
          "    }\n" +
          "  ]\n" +
          "}\n"
        },
        {
          "{ }",
          "{\"unionOptional\":null}"
        },
        {
          "{ \"unionOptional\" : { \"R1\" : { \"r1\" : \"value\" } } }",
          "{\"unionOptional\":{\"R1\":{\"r1\":\"value\"}}}"
        },
        {
          "{ \"unionOptional\" : { \"R2\" : { \"r2\" : 52 } } }",
          "{\"unionOptional\":{\"R2\":{\"r2\":52}}}"
        },
        {
          "{ \"unionOptional\" : { \"int\" : 52 } }",
          "{\"unionOptional\":{\"int\":52}}"
        },
        {
          "{ \"unionOptional\" : { \"string\" : \"value\" } }",
          "{\"unionOptional\":{\"string\":\"value\"}}"
        },
        {
          "{ \"unionOptional\" : {} }",
          "Error processing /unionOptional"
        },
      },
      {
        // record with optional union field with alias, union types are RECORD
        {
            "{\n" +
                "  \"type\" : \"record\",\n" +
                "  \"name\" : \"Foo\",\n" +
                "  \"fields\" : [\n" +
                "    {\n" +
                "      \"name\" : \"unionOptionalAlias\",\n" +
                "      \"type\" : ##T_START [\n" +
                "        { " +
                "          \"type\" : { \"type\" : \"record\", \"name\" : \"R1\", \"fields\" : [ { \"name\" : \"r1\", \"type\" : \"string\" } ] },  " +
                "          \"alias\": \"success\"" +
                "        },\n" +
                "        { " +
                "          \"type\": { \"type\" : \"record\", \"name\" : \"R2\", \"fields\" : [ { \"name\" : \"r2\", \"type\" : \"int\" } ] }, " +
                "          \"alias\": \"failure\"" +
                "        }\n" +
                "      ] ##T_END,\n" +
                "      \"optional\" : true\n" +
                "    }\n" +
                "  ]\n" +
                "}\n"
        },
        {
            "{ \"unionOptionalAlias\" : { \"success\" : { \"r1\" : \"value\" } } }",
            "{\"unionOptionalAlias\":{\"FooUnionOptionalAlias\":{\"success\":{\"R1\":{\"r1\":\"value\"}},\"failure\":null,\"fieldDiscriminator\":\"success\"}}}"
        },
        {
            "{}",
            "{\"unionOptionalAlias\":null}"
        },
        {
            "{ \"unionOptionalAlias\" : {} }",
            "Error processing /unionOptionalAlias"
        },
        {
            "{ \"unionOptionalAlias\" : { \"success\" : { \"r1\" : 123 } } }",
            "Error processing /unionOptionalAlias/success"
        }
      }
    };

    // test translation of Pegasus DataMap to Avro GenericRecord.
    for (String[][] row : inputs)
    {
      String schemaText = row[0][0];
      if (schemaText.contains("##T_START"))
      {
        assertTrue(schemaText.contains("##T_END"));
        String noTyperefSchemaText = schemaText.replace("##T_START", "").replace("##T_END", "");
        assertFalse(noTyperefSchemaText.contains("##T_"));
        assertFalse(noTyperefSchemaText.contains("typeref"));
        String typerefSchemaText = schemaText
          .replace("##T_START", "{ \"type\" : \"typeref\", \"name\" : \"Ref\", \"ref\" : ")
          .replace("##T_END", "}");
        assertFalse(typerefSchemaText.contains("##T_"));
        assertTrue(typerefSchemaText.contains("typeref"));
        testDataTranslation(noTyperefSchemaText, row);
        testDataTranslation(typerefSchemaText, row);
      }
      else
      {
        assertFalse(schemaText.contains("##"));
        testDataTranslation(schemaText, row);
      }
    }
  }

  private void testDataTranslation(String schemaText, String[][] row) throws IOException
  {
    boolean debug = false;

    if (debug) out.print(schemaText);
    RecordDataSchema recordDataSchema = (RecordDataSchema) TestUtil.dataSchemaFromString(schemaText);
    Schema avroSchema = SchemaTranslator.dataToAvroSchema(recordDataSchema);

    if (debug) out.println(avroSchema);

    // translate data
    for (int col = 1; col < row.length; col++)
    {
      String result;
      GenericRecord avroRecord = null;
      Exception exc = null;

      if (debug) out.println(col + " DataMap: " + row[col][0]);
      DataMap dataMap = TestUtil.dataMapFromString(row[col][0]);

      // translate from Pegasus to Avro
      try
      {
        avroRecord = DataTranslator.dataMapToGenericRecord(dataMap, recordDataSchema, avroSchema);
        String avroJson = AvroUtil.jsonFromGenericRecord(avroRecord);
        if (debug) out.println(col + " GenericRecord: " + avroJson);
        result = avroJson;
      }
      catch (Exception e)
      {
        exc = e;
        result = TestUtil.stringFromException(e);
        if (debug) out.println(col + " Exception: " + result);
      }

      int start = 1;
      boolean oneWay = false;
      if (start < row[col].length && row[col][start] == ONE_WAY)
      {
        oneWay = true;
        start++;
      }

      // verify
      for (int i = start; i < row[col].length; i++)
      {
        if (debug) out.println(col + " Test:" + row[col][i]);
        if (debug && exc != null && result.contains(row[col][i]) == false) exc.printStackTrace(out);
        String expectedBeforeNamespaceProcessor  = row[col][i];
        String expected = TestAvroUtil.namespaceProcessor(expectedBeforeNamespaceProcessor);
        if (debug && expected != expectedBeforeNamespaceProcessor) out.println(" Expected:" + expected);

        assertTrue(result.contains(expected));
      }

      if (avroRecord != null)
      {
        // translate from Avro back to Pegasus
        DataMap dataMapResult = DataTranslator.genericRecordToDataMap(avroRecord, recordDataSchema, avroSchema);
        ValidationResult vr = ValidateDataAgainstSchema.validate(dataMap,
                                                                 recordDataSchema,
                                                                 new ValidationOptions(RequiredMode.MUST_BE_PRESENT,
                                                                                       CoercionMode.NORMAL));
        DataMap fixedInputDataMap = (DataMap) vr.getFixed();
        assertTrue(vr.isValid());
        if (oneWay == false)
        {
          assertEquals(dataMapResult, fixedInputDataMap);
        }

        // serialize avroRecord to binary and back
        byte[] avroBytes = AvroUtil.bytesFromGenericRecord(avroRecord);
        GenericRecord avroRecordFromBytes = AvroUtil.genericRecordFromBytes(avroBytes, avroRecord.getSchema());
        byte[] avroBytesAgain = AvroUtil.bytesFromGenericRecord(avroRecordFromBytes);
        assertEquals(avroBytes, avroBytesAgain);

        // check result of roundtrip binary serialization
        DataMap dataMapFromBinaryResult = DataTranslator.genericRecordToDataMap(avroRecordFromBytes, recordDataSchema, avroSchema);
        vr = ValidateDataAgainstSchema.validate(dataMapFromBinaryResult,
                                                recordDataSchema,
                                                new ValidationOptions(RequiredMode.MUST_BE_PRESENT,
                                                                      CoercionMode.NORMAL));
        fixedInputDataMap = (DataMap) vr.getFixed();
        assertTrue(vr.isValid());
        if (oneWay == false)
        {
          assertEquals(dataMapResult, fixedInputDataMap);
        }
      }
    }
  }

  @Test
  public void testAvroSchemaMissingFields() throws IOException
  {
    final String P_SCHEMA =
            "{" +
            "  \"type\" : \"record\",\n" +
            "  \"name\" : \"Foo\",\n" +
            "  \"fields\" : [\n" +
                    "{ \"name\": \"field1\", \"type\": \"int\" }," +
                    "{ \"name\": \"field2\", \"type\": \"int\", \"optional\": true }," +
                    "{ \"name\": \"field3\", \"type\": \"int\", \"optional\": true, \"default\": 42 }," +
                    "{ \"name\": \"field4\", \"type\": \"int\", \"default\": 42 }," +
                    "{ \"name\": \"field5\", \"type\": \"null\" }" +
              "] }";
    Schema avroSchema = Schema.parse("{ \"name\": \"foo\", \"type\": \"record\", \"fields\":[]}");
    DataMap map = DataTranslator.genericRecordToDataMap(new GenericData.Record(avroSchema), (RecordDataSchema)TestUtil.dataSchemaFromString(P_SCHEMA), avroSchema);
    assertEquals(map.size(), 0);
  }

  @Test
  public void testMissingDefaultFieldsOnDataMap() throws IOException
  {
    final String SCHEMA =
        "{" +
            "   \"type\":\"record\"," +
            "   \"name\":\"Foo\"," +
            "   \"fields\":[" +
            "      {" +
            "         \"name\":\"field1\"," +
            "         \"type\":\"string\"" +
            "      }," +
            "      {" +
            "         \"name\":\"field2\"," +
            "         \"type\":{" +
            "            \"type\":\"array\"," +
            "            \"items\":\"string\"" +
            "         }," +
            "         \"default\":[ ]" +
            "      }" +
            "   ]" +
            "}";
    RecordDataSchema pegasusSchema = (RecordDataSchema)TestUtil.dataSchemaFromString(SCHEMA);
    Schema avroShema = Schema.parse(SCHEMA);
    DataMap dataMap = new DataMap();
    dataMap.put("field1", "test");
    GenericRecord record = DataTranslator.dataMapToGenericRecord(dataMap, pegasusSchema, avroShema);
    assertEquals(record.get("field2"),  new GenericData.Array<>(0, Schema.createArray(
        Schema.create(Schema.Type.STRING))));
  }

  private List<String> getList(Supplier<List<String>> supplier) {
    return supplier.get();
  }

  // can't have unchecked casts
  private static <T> T safeCast(Object toCast, Class<T> clazz) {
    if (toCast == null) {
      return null;
    }

    return Optional.of(toCast)
        .filter(clazz::isInstance)
        .map(clazz::cast)
        .orElseThrow(() -> new ClassCastException(String.format("Cast failed to class: %s for object: %s", clazz.getCanonicalName(), toCast)));
  }

  @DataProvider()
  public Object[][] arrayFieldProvider() {
    return new Object[][] {
        {
            null
        },
        {
            ImmutableList.of()
        },
        {
            ImmutableList.of("foo", "bar", "baz")
        },
        {
            getList(() -> {
              ArrayList<String> list = new ArrayList<>();
              list.addAll(ImmutableList.of("foo", "bar"));
              return list;
            })
        },
        {
            getList(() -> {
              LinkedList<String> list = new LinkedList<>();
              list.addAll(ImmutableList.of("foo", "bar"));
              return list;
            })
        },
        {
           getList(() -> {
              GenericArray<String> array = new GenericData.Array<>(1, Schema.createArray(Schema.create(Schema.Type.STRING)));
              array.add("foo");
              return array;
            })
        }
    };
  }

  @Test(dataProvider = "arrayFieldProvider", description = "generic record to data map should not care about the specific list implementation")
  public void testArrayDataTranslation(List<String> arrayFieldValue) throws IOException {
    final String arrayField = "arrayField";
    final String SCHEMA =
        "{" +
            "   \"type\":\"record\"," +
            "   \"name\":\"Foo\"," +
            "   \"fields\":[" +
            "      {" +
            "         \"name\":\"arrayField\"," +
            "         \"type\":{" +
            "            \"type\":\"array\"," +
            "            \"items\":\"string\"" +
            "         }," +
            "         \"default\":[ ]" +
            "      }" +
            "   ]" +
            "}";

    // generate generic record from data map and pegasus schema
    RecordDataSchema pegasusSchema = (RecordDataSchema)TestUtil.dataSchemaFromString(SCHEMA);
    Schema avroShema = Schema.parse(SCHEMA);
    DataMap dataMap = new DataMap();
    GenericRecord record = DataTranslator.dataMapToGenericRecord(dataMap, pegasusSchema, avroShema);

    // set array field after the fact to prevent the type from being set as GenericArray in dataMapToGenericRecord
    record.put(arrayField, arrayFieldValue);
    DataMap toTest = DataTranslator.genericRecordToDataMap(record, pegasusSchema, avroShema);

    assertEquals(safeCast(toTest.get(arrayField), List.class), arrayFieldValue);
  }
}

