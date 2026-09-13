# 8-1 · MVVM (Model, View, ViewModel)

A worked example of the MVVM architecture pattern. The packages are named after the
pattern on purpose, so the mapping is unmissable:

```
com.example.mvvm
├── model/       User.kt            StepRepository.kt
├── view/        MenuActivity.kt    MvvmActivity.kt     LoginActivity.kt
│                BrokenActivity.kt  SavedStateActivity.kt
└── viewmodel/   StepViewModel.kt   SavedStateViewModel.kt   LoginViewModel.kt
```

Real projects usually call these `data/`, `ui/` and `domain/`, and split by feature
once there is more than one screen. The names matter less than the rule: a file's
folder should tell you which of the three jobs it does.

## Four screens

| Screen | Point it makes | State lives in |
|---|---|---|
| 1 · `BrokenActivity` | what goes wrong without a ViewModel | a field on the Activity |
| 2 · `MvvmActivity` + `StepViewModel` | the same app, done with MVVM | a ViewModel |
| 3 · `SavedStateActivity` + `SavedStateViewModel` | surviving process death, not just rotation | ViewModel + `SavedStateHandle` |
| 4 · `LoginActivity` + `LoginViewModel` | a ViewModel holding rules, not just data | a ViewModel |

Screens 1 and 2 share **one layout file**. That is deliberate: the UI is identical,
so the only variable in the demo is where the data lives.

## Run it

1. Tap **+1 step** about seven times.
2. Tap **Load goal from server** and, while it still says *Loading...*, rotate
   (`Ctrl+F11` on Windows/Linux, `Cmd+←` on Mac). Check rotation is not locked.
3. Compare screen 1 and screen 2.

For screen 3: **Settings → System → Developer options → Don't keep activities**,
then leave the app and come back. Screen 2 forgets, screen 3 remembers.

For screen 4: submit an empty email, then `notanemail`, then a password under five
characters. Then open `LoginActivity.kt` and note that it contains no `if` at all.

## Reading order

```
model/StepRepository.kt      the Model     - knows HOW to fetch, nothing else
viewmodel/StepViewModel.kt   the ViewModel - screen state + logic, no Views allowed
view/MvvmActivity.kt         the View      - events down, state up
view/BrokenActivity.kt       the same app with everything in the Activity
viewmodel/LoginViewModel.kt  a ViewModel that decides something
```

Every file carries a long comment block. The comments are the teaching material.

## Notes and slides

- `notes/week08-mvvm.md` - written cheat sheet
- `notes/week08-mvvm-slides.md` - slide spec with speaker notes
- `notes/week08-mvvm-tutor-guide.md` - what the tutor needs to understand, slide by slide and file by file
