# Pattern


## Controller -> Service -> Repository pattern
- A controller should only be responsible for handling HTTP requests and responses. It should not contain any business logic or data access code.
- A service should contain the business logic of the application. It should not contain any code related to handling HTTP requests or responses, nor should it contain any data access code.
- A repository should be responsible for data access. It should not contain any business logic or code related to handling HTTP requests or responses.
- A controller should never access a repository directly. It should always go through a service.

## LOMBOK Annotations
- Use `@Data` for simple DTOs that only contain fields and getters/setters.
- Use `@Getter` and `@Setter` for more complex classes where you want to control which fields have getters and setters.
- Use `@NoArgsConstructor` and `@AllArgsConstructor` for classes that need constructors. Avoid using `@RequiredArgsConstructor` unless you have final fields that need to be initialized.

## General Java Guidelines
- Use `final` for variables that should not be reassigned after initialization.
- Use `@Override` annotation when overriding methods from a superclass or implementing methods from an interface
- `if` construct must use brackets `{}`s