from ai_manager import AIManager

class AIProcessingService:

    def process_data(self, data):
        print("AIPS: Received data:", data)

        for chunk in data:
            print("AIPS: Processing chunk: ", chunk)
            self._call_ai(chunk)
            print("AIPS: Finished processing the chunk")


    def _call_ai(self, chunk):
        aiManager = AIManager()

        res = aiManager.call("Take a look at the text provided. Respond with the first word and the last word of the chunk of text that answers a question. Provide the question as well. The chunk must be at least 50 words long",
                       f'Text: {chunk}',
                       {
                           "Question": "A question chosen chunk answers",
                           "First Word": "Word with which the chunk begins",
                           "Last Word": "Word with which the chunk ends"
                       })
        
        print("Performing action... parsing text")
        parsed_text = ""

        return {
            "Question": res['Question'],
            "Text": parsed_text
        }