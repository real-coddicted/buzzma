from contextlib import asynccontextmanager

from fastapi import FastAPI

from app.api.routes import health, score
from app.core.config import settings


@asynccontextmanager
async def lifespan(app: FastAPI):
    yield


app = FastAPI(title=settings.app_name, lifespan=lifespan)

app.include_router(health.router, tags=["health"])
app.include_router(score.router, prefix="/api/v1", tags=["score"])
