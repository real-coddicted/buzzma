from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    model_config = SettingsConfigDict(env_file=".env", env_file_encoding="utf-8")

    app_name: str = "buzzma-score"
    port: int = 8082
    log_level: str = "info"


settings = Settings()
