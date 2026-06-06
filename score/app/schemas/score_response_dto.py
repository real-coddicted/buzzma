from pydantic import BaseModel

from app.schemas.label_score import LabelScore


class ScoreResponseDto(BaseModel):
    key: str
    overall_score: float
    scores: list[LabelScore]
