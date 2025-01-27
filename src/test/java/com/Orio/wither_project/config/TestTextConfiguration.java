package com.Orio.wither_project.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Configuration
@ConfigurationProperties(prefix = "test.text")
@Getter
@Setter
public class TestTextConfiguration {
        private final String singleParagraph = """
                                    such an ideal. This is movement from product to process. This transformation in
                        conceptualization is presented in dramatic form in the Western tradition in the New
                        Testament’s description of the passion of Christ, which portrays the process and
                        consequences of revolutionary restructuring of the axioms of Western morality.
                        Christ has long been considered implicitly “contained” in the Old Testament. Frye
                        comments:
                        For Paul, Christ was mainly the concealed hero of the Old Testament
                        story and the post-Easter Christ of the resurrection. The Gospels present
                        Christ in a form that fits this pre-Gospel conception of him: not in a
                        biographical form but as a discontinuous sequence of appearances in
                        which Jesus comments on the Old Testament as a series of past events,
                        laws and images coming permanently alive in the Messianic context, and
                        body, which he supplies.554
                        What this means, at the most fundamental level of analysis, is that the pattern of action,
                        imagination and thought that Christ represents is necessarily “there” in any narrative or
                        mythology, sufficiently compelling to embed itself in memory. The reasons for this
                        implicit existence are clear, in a sense: Christ embodies the hero, grounded in tradition,
                        who is narrative depiction of the basis for successful individual and social adaptation. As
                        the Word “made flesh” (John 1:14) there “in the beginning” (John 1:1), he represents,
                        simultaneously, the power that divides order from chaos, and tradition rendered spiritual,
                        abstract, declarative, semantic. His manner of being is that which moves morality itself
                        from rule of law to rule of spirit—which means process. Spirit is process, simultaneously
                        opposed to and responsible for generating static being. Frye states:
                        We are told in the New Testament itself that the mysteries of faith have to
                        be “spiritually discerned” (I Corinthians 2:14). This is in a passage where
                        Paul is contrasting the letter, which he says “killeth,” with the spirit that
                        “giveth life.”555
                        This idea is represented schematically in Figure 60: The Emergence of Christ from
                        Group Identity and Chaos, which also portrays the Christian “story of man.”
                        For Christ, “God is not the God of the dead, but of the living” (Matthew 22:32). Christ
                        pushes morality beyond strict reliance on codified tradition—the explicit Law of
                        Moses—not because such tradition was unnecessary, but because it was (and is)
                        necessarily and eternally insufficient. He states:
                        For I say unto you, That except your righteousness exceed the
                        righteousness of the scribes and the Pharisees, ye shall in no case enter
                        into the kingdom of heaven. (Matthew 5:20)
                        but also
                        Think not that I am come to destroy the law, or the prophets: I am not
                        come to destroy, but to fulfil. (Matthew 5:17)
                                    """;;
        private final String pageSummaries = """
                        The passage explores the culmination of the story of the Grand Inquisitor from Dostoyevsky's The Brothers Karamazov. The prisoner (Christ) responds to the Grand Inquisitor’s accusations with a silent and symbolic kiss, highlighting themes of love and forgiveness transcending ideological conflict. This act unsettles the Inquisitor, who ultimately releases Christ, instructing Him never to return. The narrative underscores the tension between individual impulse and community structure, reflecting on the transformative power of individual actions within a cultural framework.\n\nThis section delves into the nature of myth as the encoded memory of human behavior and cultural evolution. Myths serve as dramatic narratives, synthesizing past experiences and offering guidance for moral and adaptive behaviors. They provide a blueprint for handling the tension between chaos and order, acting as cultural tools that protect individuals from existential fear while fostering creativity and exploration. Myths connect historical continuity with individual and collective action, emphasizing the dynamic interplay between stability and transformation.\n\nThe text examines the role of heroism in shaping cultural and moral frameworks. Heroic behavior embodies adaptive processes and serves as a model for imitation, evolving over time into codified cultural behaviors. This process integrates tradition and adaptation, balancing individual and societal needs. The narrative suggests that as societies evolve, they shift focus from the products of heroism (e.g., cultural achievements) to the act of heroism itself, reflecting an increasing abstraction and conscious understanding of moral and adaptive behaviors.
                        """;;
        private final String chapterSummaries = """
                                    In *Maps of Meaning*, Jordan B. Peterson provides a profound exploration of how humans construct meaning through myths, narratives, and archetypal symbols. He begins by distinguishing between two ways of perceiving the world: as a place of objects, representing objective reality studied by science, and as a forum for action, the subjective realm where meaning and experience reside. While science describes what is, it does not offer moral or existential guidance. Instead, narratives—myths, stories, and religious teachings—fulfill this role, conveying lessons that help individuals navigate life’s complexities.

                        Peterson then delves into the neuropsychological underpinnings of meaning-making, emphasizing how the brain balances order (the known) and chaos (the unknown). He introduces archetypal symbols to illustrate this dynamic. The Great Mother represents chaos, potential, and the unknown, while the Great Father symbolizes order, tradition, and structure. The Divine Son, or hero archetype, confronts chaos to renew and transform. These archetypes, rooted in Jungian psychology, reflect universal patterns through which humans interpret experiences and craft meaning.

                        In examining how individuals are socialized into their cultures, Peterson explores the role of cultural narratives and traditions in providing a framework for understanding the world. These narratives offer protection from the uncertainties of the unknown but also carry the risk of becoming rigid and outdated. He advocates for a balance, urging individuals to adopt the hero archetype by challenging and updating cultural norms to foster growth and adaptation.

                        """;;

        private final String shortTechnical = """
                        Epistemology is the branch of philosophy that examines the nature, origin, and limits of knowledge. Also called theory of knowledge, it explores different types of knowledge, such as propositional knowledge about facts, practical knowledge in the form of skills, and knowledge by acquaintance as a familiarity through experience. Epistemologists study the concepts of belief, truth, and justification to understand the nature of knowledge. To discover how knowledge arises, they investigate sources of justification, such as perception, introspection, memory, reason, and testimony.
                        """;
}
