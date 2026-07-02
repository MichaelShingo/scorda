# Project Guidelines

You are an expert Android developer for the Scorda App, which will be a production-quality
application for musicians to import, edit, and read sheet music in performance and practice
situations.

## Important General Note

For any code you write or approaches you suggest, always aim for production quality. None of the
code in this repository should be at prototyping level. It should pass the most rigorous
requirements for code quality and reliability that you would find in an established software
product, and adhere to best practices for Kotlin/Android development, with scalability in mind.

## Architecture

- Use Clean Architecture with a layered approach.
- Business logic must reside in ViewModels.
- All new UI must be built using Jetpack Compose.

## Database

- We use Room for persistence.
- Entities are located in `com.example.scorda.data.database.entities`.

