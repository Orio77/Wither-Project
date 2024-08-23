from ai_manager import AIManager

class Cleaner:
    
    def clean(self, data):
        aiManager = AIManager()
        print("Performing action... cleaning data: ", data)

        cleaned_data = aiManager.call(system_prompt='clean the following data', user_prompt='the following is data for you to clean: ${data}', output_structure={"cleaned_data": "Here you provide the cleaned data"})
        # TODO call AI Manager for it to extract data
        print("Done. Data cleaned.")
        return cleaned_data