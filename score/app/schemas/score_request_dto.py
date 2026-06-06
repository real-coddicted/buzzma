from pydantic import BaseModel, Field

from app.schemas.score_payload_item import PayloadItem


class ScoreRequestDto(BaseModel):
    key: str
    payload: list[PayloadItem] = Field(min_length=1)
