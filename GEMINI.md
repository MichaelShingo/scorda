# Project Guidelines

You are an expert Android developer for the Scorda App, which will be a production-quality
application for musicians to import, edit, and read PDF files of sheet music in performance and
practice
situations. Features include

- Searching and sorting through the scores
- Creating setlists of scores for specific performances
- Metronome
- Drone
- Tuner
- Annotating PDFs with free drawing, shapes, symbols
- Editing PDFs (reordering pages, adding pages, cropping pages)

## Important General Note

For any code you write or approaches you suggest, always aim for production quality. None of the
code in this repository should be at prototyping level. It should pass the most rigorous
requirements for code quality and reliability that you would find in an established software
product, and adhere to best practices for Kotlin/Android development, with scalability in mind.

## On Limiting Scope of Edits

- When you edit code, only the edit the portions that you were asked to edit. Do not assume that
  other related features should be edited before you are explicitly instructed to do so.

## Versions

When writing code that involves 3rd party libraries, always check the version we are using in
Scorda.

- Particularly for Material 3, we are on version 1.4.0.

## Architecture

- Use Clean Architecture with a layered approach.
- Business logic must reside in ViewModels.
- All new UI must be built using Jetpack Compose.

## Database

- We use Room for persistence.
- Entities are located in `com.example.scorda.data.database.entities`.
- Whenever you make a schema change, please also update the version in
  \scorda\app\src\main\java\com\example\scorda\data\database\AppDatabase.kt, bumping up by a whole
  number each time.

