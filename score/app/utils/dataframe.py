import pandas as pd

from app.schemas.score_request_dto import ScoreRequestDto


def score_request_to_df(request: ScoreRequestDto) -> pd.DataFrame:
    return pd.DataFrame([
        {"key": request.key, "label": item.label, "expected": item.expected, "actual": item.actual, "weight": item.weight}
        for item in request.payload
    ])