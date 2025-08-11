import pandas as pd
from datetime import datetime
from pykrx import stock
import json
import os

# ---------- 날짜 유틸: 어제(영업일)와 한 달 전(영업일) ----------
def get_business_dates_one_month():
    # 서울 기준 오늘 자정으로 맞춘 뒤 tz 제거
    today = pd.Timestamp.now(tz="Asia/Seoul").normalize().tz_localize(None)

    # 어제 = 영업일 기준 하루 전 (토/일 자동 스킵)
    yesterday = today - pd.tseries.offsets.BDay(1)

    # 한 달 전
    one_month_before = yesterday - pd.DateOffset(months=3)

    # 한 달 전이 주말이면 이전 영업일로 당겨서 보정
    while one_month_before.weekday() >= 5:  # 5=토, 6=일
        one_month_before -= pd.tseries.offsets.BDay(1)

    return one_month_before.strftime("%Y%m%d"), yesterday.strftime("%Y%m%d")

# ---------- 티커 목록 ----------
tickers = [
    "005930", "000660", "373220", "207940", "005380", "012450", "105560", "000270", "034020", "068270",
    "329180", "035420", "055550", "028260", "012330", "011170", "042660", "005490", "011200", "086790",
    "015760", "035720", "009540", "032830", "006400", "009150", "018260", "086280", "017670", "096770",
    "034730", "051910", "034220", "051900", "066570", "011070", "032640", "047050", "456040", "010140",
    "009830", "017800", "272210", "000880", "000720", "128940", "302440", "285130", "326030", "377300",
    "196170", "247540", "003380", "086520", "028300", "214450", "000250", "084990", "277070", "145020",
    "298380", "214150", "058470", "950160", "214370", "257720", "041510", "039030", "206650", "035900",
    "082270", "068760", "263750", "403870", "237690", "065350", "140860", "007390", "357780", "096530",
    "347850", "122870", "005290", "035760", "293490", "253450", "328130", "036930", "240810", "348370",
    "039200", "358570", "018290", "095340", "178320", "083650", "195940", "376300", "140410", "078600"
]

# ---------- 날짜 자동 설정 ----------
start_date, end_date = get_business_dates_one_month()
print("start_date =", start_date, "/ end_date =", end_date)

# ---------- 결과 저장 구조 ----------
result_json = {}
output_path = "./data/stock/output/returns_data.json"
os.makedirs(os.path.dirname(output_path), exist_ok=True)

# ---------- 데이터 수집 ----------
for code in tickers:
    try:
        # 권장 함수: by_date
        df = stock.get_market_ohlcv_by_date(start_date, end_date, code)
        if df is None or df.empty:
            result_json[code] = {}
            continue

        # 종가만 추출
        close_prices = df["종가"]

        # NaN 안전 처리 + int 캐스팅
        close_dict = {}
        for k, v in close_prices.items():
            if pd.notnull(v):
                close_dict[k.strftime("%Y-%m-%d")] = int(v)

        result_json[code] = close_dict
    except Exception as e:
        print(f"{code} 오류 발생: {e}")
        result_json[code] = {}

# ---------- JSON 저장 ----------
with open(output_path, "w", encoding="utf-8-sig") as f:
    json.dump(result_json, f, ensure_ascii=False, indent=2)

print("returns_data.json 저장 완료:", output_path)
