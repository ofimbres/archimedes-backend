Deploy your React projects to AWS Elastic Beanstalk using CI/CD AWS CodePipeline (Part 1)
https://dev.to/ogambakerubo/deploy-your-react-projects-to-aws-elastic-beanstalk-using-cicd-aws-codepipeline-part-1-1nne

How to model one-to-many relationships in DynamoDB
https://www.alexdebrie.com/posts/dynamodb-one-to-many/
https://www.serverlesslife.com/DynamoDB_Design_Patterns_for_Single_Table_Design.html

composite keys
https://amazon-dynamodb-labs.com/design-patterns/ex6compos.html


Modify Health Check Path in Elastic Beanstalk
Configuration -> Load Balancer -> Processes

https://aws.plainenglish.io/making-elastic-beanstalk-deployment-production-ready-b21a99319ea

https://www.honeybadger.io/blog/node-elastic-beanstalk/

---

## Project Structure

This Spring Boot application follows a clean architecture with the following package organization:

```
src/main/java/com/binomiaux/archimedes/
├── app/                          # Application layer
│   ├── ArchimedesApplication.java   # Main Spring Boot application
│   └── controller/                  # REST controllers
├── config/                       # Configuration classes
│   ├── aws/                         # AWS service configurations
│   ├── web/                         # Web/security configurations
│   └── *.java                       # General configurations (Jackson, OpenAPI, etc.)
├── dto/                          # Data Transfer Objects
│   ├── request/                     # Request DTOs (API input)
│   └── response/                    # Response DTOs (API output)
├── exception/                    # Custom exceptions and error handling
│   ├── business/                    # Business logic specific exceptions
│   └── common/                      # General-purpose exceptions
├── model/                        # Domain models (unified entities/models)
├── repository/                   # Data access layer (DynamoDB)
└── service/                      # Business logic layer
```

### Key Design Decisions

- **Unified Models**: Combined DynamoDB entities and domain models into single classes
- **No MapStruct**: Simplified mapping by using direct object creation or simple utilities
- **Java Records**: Used for simple immutable DTOs where appropriate
- **Constructor Injection**: Preferred over field injection for better testability
- **Clean Separation**: Controllers handle HTTP concerns, services contain business logic

### Technology Stack

- **Java 21** with Spring Boot 3.x
- **DynamoDB** for data persistence
- **Gradle** for build management
- **AWS SDK** for cloud services integration