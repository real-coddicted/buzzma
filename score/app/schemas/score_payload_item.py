from pydantic import BaseModel


class                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   PayloadItem(BaseModel):
    label: str
    expected: str
    actual: str
    weight: float = 1.0
