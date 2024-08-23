package com.Orio.web_scraping_tool.service.dataProcessing;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.Orio.web_scraping_tool.model.DataModel;
import com.Orio.web_scraping_tool.service.newImpl.dataProcessing.OllamaQAService;

@SpringBootTest
public class OllamaQAServiceTest {

    @Autowired
    private OllamaQAService ollamaQAService;
    private static List<DataModel> dataList;

    @BeforeAll
    private static void setUpTestData() {
        String wikiThoughtTwoParagraphs = """
                                    Platonism
                According to Platonism, thinking is a spiritual activity in which Platonic forms and their interrelations are discerned and inspected.[22][23] This activity is understood as a form of silent inner speech in which the soul talks to itself.[24] Platonic forms are seen as universals that exist in a changeless realm different from the sensible world. Examples include the forms of goodness, beauty, unity, and sameness.[25][26][27] On this view, the difficulty of thinking consists in being unable to grasp the Platonic forms and to distinguish them as the original from the mere imitations found in the sensory world. This means, for example, distinguishing beauty itself from derivative images of beauty.[23] One problem for this view is to explain how humans can learn and think about Platonic forms belonging to a different realm.[22] Plato himself tries to solve this problem through his theory of recollection, according to which the soul already was in contact with the Platonic forms before and is therefore able to remember what they are like.[23] But this explanation depends on various assumptions usually not accepted in contemporary thought.[23]

                Aristotelianism and conceptualism
                Aristotelians hold that the mind is able to think about something by instantiating the essence of the object of thought.[22] So while thinking about trees, the mind instantiates tree-ness. This instantiation does not happen in matter, as is the case for actual trees, but in mind, though the universal essence instantiated in both cases is the same.[22] In contrast to Platonism, these universals are not understood as Platonic forms existing in a changeless intelligible world.[28] Instead, they only exist to the extent that they are instantiated. The mind learns to discriminate universals through abstraction from experience.[29] This explanation avoids various of the objections raised against Platonism.[28]

                Conceptualism is closely related to Aristotelianism. It states that thinking consists in mentally evoking concepts. Some of these concepts may be innate, but most have to be learned through abstraction from sense experience before they can be used in thought.[22]

                It has been argued against these views that they have problems in accounting for the logical form of thought. For example, to think that it will either rain or snow, it is not sufficient to instantiate the essences of rain and snow or to evoke the corresponding concepts. The reason for this is that the disjunctive relation between the rain and the snow is not captured this way.[22] Another problem shared by these positions is the difficulty of giving a satisfying account of how essences or concepts are learned by the mind through abstraction.
                                """;

        String wikiThoughtPartialIdea = """
                                    often ascribed to inner speech: it is in an important sense similar to hearing sounds, it involves the use of language and it constitutes a motor plan that could be used for actual speech.[24] This connection to language is supported by the fact that thinking is often accompanied by muscle activity in the speech organs. This activity may facilitate thinking in certain cases but is not necessary for it in general.[1] According to some accounts, thinking happens not in a regular language, like English or French, but has its own type of language with the corresponding symbols and syntax. This theory is known as the language of thought hypothesis.[30][32]

                Inner speech theory has a strong initial plausibility since introspection suggests that indeed many thoughts are accompanied by inner speech. But its opponents usually contend that this is not true for all types of thinking.[22][5][33] It has been argued, for example, that forms of daydreaming constitute non-linguistic thought.[34] This issue is relevant to the question of whether animals have the capacity to think. If thinking is necessarily tied to language then this would suggest that there is an important gap between humans and
                                """;

        String chickenRecipe = """
                                    Ingredients
                6 (3 ounce) packages chicken flavored ramen noodles

                2 (15 ounce) jars Alfredo sauce

                3 cups shredded cooked chicken, from one rotisserie chicken

                1 cup water

                1 cup heavy cream

                2 cups shredded Italian blend cheese

                1/2 teaspoon ground black pepper

                Local Offers

                Change
                Oops! We cannot find any ingredients on sale near you. Do we have the correct zip code?
                Directions
                Preheat the oven to 350 degrees F (175 degrees C). Arrange uncooked ramen noodles in an even layer in the bottom of a large casserole dish, breaking noodle blocks as necessary to fit. Sprinkle 1 packet of seasoning evenly over noodles; discard remaining seasoning packets.

                Pour 1 jar of Alfredo sauce evenly over noodles. Spread chicken evenly over sauce. Pour second jar of Alfredo sauce over chicken. Add water to first Alfredo jar, shake it to rinse sauce from the jar, and pour mixture over noodles. Add cream to second jar, shake, and pour over the casserole.  Top with cheese and pepper.

                Bake in the preheated oven until browned and bubbly and noodles are tender, 50 to 60 minutes. Add up to 1/2 cup more water as needed, if too much is absorbed before noodles are cooked.

                I Made It
                Print
                Nutrition Facts (per serving)
                1180
                Calories
                103g
                Fat
                20g
                Carbs
                46g
                Protein

                Show Full Nutrition Label



                Reviews (2)
                Check out our Community Guidelines about reviews.
                                """;

        dataList = new ArrayList<>(Arrays.asList(new DataModel("wikiThoughtTwoParagraphs", wikiThoughtTwoParagraphs),
                new DataModel("wikiThoughtPartialIdea", wikiThoughtPartialIdea),
                new DataModel("chickenRecipe", chickenRecipe)));

    }

    @Test
    public void testGenerateQuestions() {
        // Call the method
        ollamaQAService.generateQuestions(dataList);

        // Assertions
        int validDataModelCount = 0;
        for (DataModel dataModel : dataList) {
            if (dataModel.getQuestion() != null && dataModel.getAnswer() != null &&
                    dataModel.getQuestion().length() > 0 && dataModel.getAnswer().length() > 0) {
                validDataModelCount++;
            }
        }
        assertTrue(validDataModelCount >= 2, "There should be at least two valid DataModels");
    }
}