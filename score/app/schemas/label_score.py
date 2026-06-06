from pydantic import BaseModel


class LabelScore(BaseModel):
    label: str
    score: float
