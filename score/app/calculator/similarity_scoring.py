import pandas as pd
from rapidfuzz import fuzz


def compute_text_similarity(df: pd.DataFrame) -> pd.DataFrame:
    df = df.copy()
    df["score"] = df.apply(lambda row: _similarity(row["expected"], row["actual"]), axis=1)
    return df


def _similarity(expected: str, actual: str) -> float:
    expected_clean = str(expected).lower().strip()
    actual_clean = str(actual).lower().strip()

    if not expected_clean or not actual_clean:
        return 0.0

    return round(fuzz.token_set_ratio(expected_clean, actual_clean) / 100.0, 4)
