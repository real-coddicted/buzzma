import pandas as pd
from fastapi import APIRouter, HTTPException, Query

from app.schemas import ScoreRequestDto, ScoreResponseDto
from app.schemas.label_score import LabelScore
from app.calculator.overall_scoring import compute_overall_score
from app.calculator.scoring_algorithm import ScoringAlgorithm
from app.calculator.similarity_scoring import compute_text_similarity
from app.utils.dataframe import score_request_to_df

router = APIRouter()


@router.post("/score", response_model=list[ScoreResponseDto])
def score(
    requests: list[ScoreRequestDto],
    algorithm: ScoringAlgorithm = Query(default=ScoringAlgorithm.WEIGHTED_AVERAGE),
) -> list[ScoreResponseDto]:
    if not requests:
        return []

    keys = [r.key for r in requests]
    duplicates = {k for k in keys if keys.count(k) > 1}
    if duplicates:
        raise HTTPException(status_code=422, detail=f"Duplicate keys: {sorted(duplicates)}")

    try:
        df = pd.concat([score_request_to_df(r) for r in requests], ignore_index=True)
        df = compute_text_similarity(df)
        overall = compute_overall_score(df, algorithm).set_index("key")["overall_score"]

        return [
            ScoreResponseDto(
                key=key,
                overall_score=overall[key],
                scores=[LabelScore(label=row["label"], score=row["score"]) for _, row in group.iterrows()],
            )
            for key, group in df.groupby("key")
        ]
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Scoring failed: {e}") from e