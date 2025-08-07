import pandas as pd
import json
import os
import sys
from datetime import datetime, timedelta
from pykrx import stock, bond

### ✅ SMB 계산 ###
def get_monthly_returns(start_date, end_date):
    tickers = stock.get_market_ticker_list(end_date, market="ALL")
    data = []

    for ticker in tickers:
        try:
            df = stock.get_market_ohlcv_by_date(start_date, end_date, ticker, freq='m')
            if df.empty or len(df) < 2:
                continue

            df['수익률'] = df['종가'].pct_change()
            df['종목코드'] = ticker
            df['종목명'] = stock.get_market_ticker_name(ticker)

            market_cap = stock.get_market_cap_by_date(start_date, end_date, ticker, freq='m')
            if not market_cap.empty:
                df['시가총액'] = market_cap['시가총액']
                data.append(df)
        except:
            continue

    return pd.concat(data) if data else pd.DataFrame()

def calculate_smb(date: str):
    prev_month = (datetime.strptime(date, "%Y%m%d") - timedelta(days=40)).strftime("%Y%m%d")

    print(f"[DEBUG] 분석 기준일: {date}, 이전 기준일: {prev_month}")
    df = get_monthly_returns(prev_month, date)
    print(f"[DEBUG] get_monthly_returns 반환 shape: {df.shape}")
    print(f"[DEBUG] get_monthly_returns 반환 샘플:\n{df.head()}")

    if df.empty:
        raise ValueError("데이터가 비어있습니다.")

    latest = df.reset_index().groupby('종목코드').last()
    latest = latest.dropna(subset=['수익률', '시가총액'])

    latest_sorted = latest.sort_values(by='시가총액')
    midpoint = len(latest_sorted) // 2
    small = latest_sorted.iloc[:midpoint]
    big = latest_sorted.iloc[midpoint:]

    smb = small['수익률'].mean() - big['수익률'].mean()
    return round(smb, 6)

def calculate_hml(date: str) -> float | None:
    # 👉 기준일에서 30일 전 날짜 계산
    def get_prev_month_date(date_str):
        date_obj = datetime.strptime(date_str, "%Y%m%d")
        prev_date = date_obj - timedelta(days=30)
        return prev_date.strftime("%Y%m%d")

    # 👉 개별 종목의 1개월 수익률 계산
    def get_return(ticker):
        try:
            df = stock.get_market_ohlcv_by_date(prev_month, date, ticker)
            if df.empty or len(df) < 2:
                return None
            return (df['종가'].iloc[-1] - df['종가'].iloc[0]) / df['종가'].iloc[0]
        except Exception as e:
            print(f"[ERROR] {ticker} 수익률 계산 실패: {e}")
            return None

    try:
        # ✅ 이전 기준일 설정
        prev_month = get_prev_month_date(date)

        # ✅ 펀더멘털 데이터 수집
        df = stock.get_market_fundamental_by_ticker(date, market="KOSPI")
        if 'PBR' not in df.columns:
            print(f"[ERROR] {date}: 'PBR' 컬럼 없음")
            return None

        # ✅ 결측값 및 이상치 제거
        df = df[df['PBR'].notna() & (df['PBR'] > 0)]
        df = df.reset_index().rename(columns={"티커": "종목코드"})
        df['티커'] = df['종목코드']

        if len(df) < 30:
            print(f"[WARNING] {date}: 유효한 종목 수 부족 (N={len(df)})")
            return None

        # ✅ PBR 기준 정렬
        df_sorted = df.sort_values('PBR')
        n = len(df_sorted)

        # ✅ 그룹 나누기
        low = df_sorted.iloc[:n // 3].copy()    # PBR 낮은 그룹 = 가치주
        high = df_sorted.iloc[-n // 3:].copy()  # PBR 높은 그룹 = 성장주

        # ✅ 수익률 계산
        low['return'] = low['티커'].apply(get_return)
        high['return'] = high['티커'].apply(get_return)

        # ✅ 유효한 수익률 필터링
        low_ret = low['return'].dropna()
        high_ret = high['return'].dropna()

        if len(low_ret) < 5 or len(high_ret) < 5:
            print(f"[WARNING] {date}: 수익률 데이터 부족 (low={len(low_ret)}, high={len(high_ret)})")
            return None

        # ✅ HML 계산
        hml = round(low_ret.mean() - high_ret.mean(), 6)
        print(f"[INFO] {date} HML = {hml:.6f}")
        return hml

    except Exception as e:
        print(f"[ERROR] {date} HML 계산 중 예외 발생: {e}")
        return None

from pykrx import stock, bond

### ✅ 시장 수익률 계산
def get_market_return(start_date: str, end_date: str, index_code: str):
    df = stock.get_index_ohlcv_by_date(start_date, end_date, index_code, freq='m')
    df['수익률'] = df['종가'].pct_change()
    return df

### ✅ 무위험 수익률 (Rf) 계산: 국고채 1년 기준
def get_risk_free_rate(date: str, max_retry_days: int = 15):
    base_date = datetime.strptime(date, "%Y%m%d")

    for i in range(max_retry_days):
        try_date = (base_date - timedelta(days=i)).strftime("%Y%m%d")
        try:
            df = bond.get_otc_treasury_yields(try_date)
            if df.empty:
                continue

            # ✅ 국고채 1년만 추출
            for idx in df.index:
                if "국고채 1년" in idx:
                    return float(df.loc[idx, "수익률"]) / 100

        except Exception as e:
            print(f"[무위험수익률 조회 오류] {try_date}: {e}")
            continue

    # max_retry_days 동안 수익률을 찾지 못한 경우
    print(f"[WARN] {date} 기준 무위험 수익률을 찾지 못했습니다.")
    return None

### ✅ MKT - RF 계산
def calculate_mkt_rf_all(start_date: str, end_date: str, rf_date: str):
    rf = get_risk_free_rate(rf_date, max_retry_days=15)
    if rf is None:
        raise ValueError(f"{rf_date} 기준 무위험 수익률을 찾을 수 없습니다.")

    mkt_rf = {}
    for market, index_code in {"kospi": "1001", "kosdaq": "2001"}.items():
        market_df = get_market_return(start_date, end_date, index_code)
        if market_df.empty:
            raise ValueError(f"{market.upper()} 지수 데이터가 없습니다.")

        market_return = market_df['종가'].iloc[-1] / market_df['종가'].iloc[0] - 1
        mkt_rf[market] = round(market_return - rf, 6)

    return mkt_rf

def calculate_mom(date: str):
    # 기준일로부터 3개월 전 날짜 계산
    start_date = (datetime.strptime(date, "%Y%m%d") - timedelta(days=90)).strftime("%Y%m%d")
    tickers = stock.get_market_ticker_list(date, market="ALL")

    momentum_data = []

    for ticker in tickers:
        try:
            df = stock.get_market_ohlcv_by_date(start_date, date, ticker)
            if df.empty or len(df) < 2:
                continue

            # 3개월 수익률 계산
            ret = (df['종가'].iloc[-1] - df['종가'].iloc[0]) / df['종가'].iloc[0]
            momentum_data.append((ticker, ret))
        except Exception as e:
            print(f"[MOM] {ticker} 처리 중 오류 발생: {e}")
            continue

    # 유효 종목이 없는 경우
    if not momentum_data or len(momentum_data) < 10:
        print(f"[MOM] 유효한 종목 수 부족: {len(momentum_data)}개. 모멘텀 계산 생략.")
        return None  # 또는 return 0.0

    # 수익률 기준 정렬
    sorted_data = sorted(momentum_data, key=lambda x: x[1], reverse=True)
    n = len(sorted_data)

    top30 = sorted_data[:int(n * 0.3)]
    bottom30 = sorted_data[-int(n * 0.3):]

    # 평균 수익률 계산
    try:
        top_avg = sum([x[1] for x in top30]) / len(top30)
        bottom_avg = sum([x[1] for x in bottom30]) / len(bottom30)
    except ZeroDivisionError:
        print("[MOM] 상위 또는 하위 그룹 평균 계산 실패 (0개)")
        return None

    mom = round(top_avg - bottom_avg, 6)
    print(f"[MOM] {date} → 종목수: {len(momentum_data)}, MOM: {mom}")
    return mom

### ✅ 저장 ###
def save_factors_json(date_str, smb_value, hml_value, mom_value, mkt_rf_dict, output_path="./data/stock/output/factor_result.json"):
    result = {
        "date": date_str,
        "smb": smb_value,
        "hml": hml_value,
        "mom": mom_value,
        "kospi": mkt_rf_dict.get("kospi"),
        "kosdaq": mkt_rf_dict.get("kosdaq")
    }
    os.makedirs(os.path.dirname(output_path), exist_ok=True)
    with open(output_path, "w", encoding="utf-8-sig") as f:
        json.dump(result, f, indent=2, ensure_ascii=False)

### ✅ 실행 ###
if __name__ == "__main__":
    # 기준일 설정
    analysis_date = sys.argv[1]
    result_date = sys.argv[2]
    mkt_start = sys.argv[3]
    output_path = "./data/stock/output/factor_result.json"

    # 계산
    smb = calculate_smb(analysis_date)
    hml = calculate_hml(analysis_date)
    mom = calculate_mom(analysis_date)
    mkt_rf_dict = calculate_mkt_rf_all(mkt_start, analysis_date, result_date)

    # 저장
    save_factors_json(result_date, smb, hml, mom, mkt_rf_dict, output_path)

    print(f"SMB: {smb}, HML: {hml}, MOM: {mom}, KOSPI: {mkt_rf_dict['kospi']}, KOSDAQ: {mkt_rf_dict['kosdaq']} → 저장 완료: {output_path}")
