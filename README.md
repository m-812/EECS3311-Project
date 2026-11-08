# AI Smart Recipe and Meal Planning Agent

EECS 3311 project: a Java desktop app with a Swing GUI and a command-line interface. It uses Claude Haiku 5.5 to:
- recommend recipes
- organise recipes you paste in
- suggest changes to a recipe
- search your saved recipes
- plan a week of meals
- build a shopping list

## Prerequisites

- Java 17 or newer (a JDK)
- Maven 3.9 or newer
- An Anthropic API key, for the AI features

## Setup

1. Clone the repository and open a terminal in its folder.
2. Copy `.env.example` to `.env` and put your key after `LLM_API_KEY=`. `.env` is git-ignored, so never commit it.
3. Build the project. Maven downloads the only dependency, Gson.
   ```
   mvn compile
   ```

## Run

- GUI: `mvn compile exec:java`
- CLI: `mvn compile exec:java -Dexec.args=--cli`

To start with 26 sample recipes already saved, from [sample-data/fixtures.json](sample-data/fixtures.json), add `--fixtures`:
- GUI: `mvn compile exec:java -Dexec.args=--fixtures`
- CLI: `mvn compile exec:java -Dexec.args="--cli --fixtures"`

Without a key, the app still starts. The non-AI features work, and the AI features show an error.

## Example usage

The inputs below come from [sample-data/demo-data.txt](sample-data/demo-data.txt).

1. **Profile tab:** set "Dietary restrictions" to `vegetarian` and save.
2. **Pantry tab:** add `Eggs` with quantity `6`.
3. **Recipes tab → Add personal recipe:** paste one of the sample recipes. The AI splits it into fields and saves it.
4. **Recommend tab:** ask for `A quick dinner using eggs and spinach`, then save one of the suggestions.
5. **Recipes tab:**
   - Select a recipe and favourite it.
   - Choose Edit and ask the AI `How can I make this serve 4?`, then Save.
   - Search for `Something I can eat for breakfast`.
6. **Meal Plan tab:** click a slot to choose a recipe, or generate a plan with `High protein, nothing over 30 minutes`. Then make a shopping list.

The CLI has the same features under numbered menus. Where you type several lines, such as a pasted recipe, finish with a line reading `END`.

## Notes

- Data is kept in memory only, so it is lost when the app closes.
- The source code is in `src/main/java/recipeagent/`.
