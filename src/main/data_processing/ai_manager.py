import ollama
import json
import os

class AIManager:

    def call(self, system_prompt, user_prompt):
        print("AIMGR: Performing action... Calling ai with: ", system_prompt, " ", user_prompt)

        response = ollama.chat(model='llama3.1:8b', messages=[{
            "role": 'system',
            "content": system_prompt,
        },{
            "role": "user",
            "content": user_prompt
        }], format='')

        if response:
            print("Done. AI Called and Responded")
            response_file_path = 'response.json'
            # Check if response.json exists, if not create it
            file_path = os.path.join(os.path.dirname(__file__), response_file_path)
            with open(file_path, 'w', encoding='utf-8') as f:
                json.dump(response['message']['content'], f, ensure_ascii=False, indent=4)
        else:
            print("An error occurred and AI failed to respond")

        print(response['message']['content'])

        return []

if __name__ == "__main__":
    ai_manager = AIManager()
    # Define the full path to data.json
    data_json_path = r'C:\Users\macie\tools\web-scraping-tool\src\main\data_processing\short_data.json'
    print("SHORTY: ", data_json_path)
    
    # Read data from data.json
    with open(data_json_path, 'r') as f:
        data = json.load(f)

    res = ai_manager.call("The following text contains an article within, buried in bunch of html and css. Your job is to extract the article and provide it to me in a readable format.",
                       f'Text: {data}',#["https://eu.pressconnects.com/story/news/local/2019/03/18/ask-scientist-how-do-thoughts-work-our-brain/3153303002/"]}',
                       )