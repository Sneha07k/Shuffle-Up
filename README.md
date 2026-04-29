# CardGame

A two-player Java card game with a Swing GUI. Play tricks against an AI opponent with follow-suit rules and a pick-up mechanic across three difficulty levels.

---

## How to Play

Each round, one player leads by playing a card. The other must follow suit if they can. If they can't, they play any card , but they pick up both cards as a penalty instead of winning the trick. If both players follow suit, the higher card wins the trick. The player who empties their hand first wins.

---

## Difficulty Levels

| Level | Behaviour |
|-------|-----------|
| Easy | Plays randomly |
| Medium | Leads high cards, follows suit intelligently |
| Hard | Tracks seen cards, detects suit weaknesses, scores moves with look-ahead |

---

## Requirements

- Java 17 or higher
- No external dependencies

---

## Running the Game

**From source:**
```bash
javac *.java
java GameGUI
```


## Project Structure

| File | Description |
|------|-------------|
| `GameGUI.java` | Swing UI and game flow |
| `GameEngine.java` | Turn logic, legal move enforcement, trick resolution |
| `GameState.java` | All mutable game state |
| `AIEngine.java` | AI decision making across difficulty levels |
| `Card.java` | Card model with suit and rank |
| `Deck.java` | Standard 52-card deck with shuffle and deal |
| `Difficulty.java` | Difficulty enum |

---

Screenshots:
<img width="386" height="172" alt="image" src="https://github.com/user-attachments/assets/373fad39-999c-45ea-807c-35aff8b2db6b" />

<img width="1328" height="1037" alt="image" src="https://github.com/user-attachments/assets/f8f6f3f3-7a2b-40fb-9bdc-a7f176e19d88" />

<img width="1322" height="1029" alt="image" src="https://github.com/user-attachments/assets/dc009a4e-d619-4e6a-8d4f-26c361da2604" />

<img width="1323" height="1023" alt="image" src="https://github.com/user-attachments/assets/4af184ce-96a7-402a-ab98-a311a263b70e" />

