import pandas as pd
import json
import os
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
    df = get_monthly_returns(prev_month, date)

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

### ✅ HML 계산 ###
def calculate_hml(date: str):
    prev_month = (datetime.strptime(date, "%Y%m%d") - timedelta(days=40)).strftime("%Y%m%d")
    # 1. PBR 데이터 수집
    df_kospi = stock.get_market_fundamental_by_ticker(date, market="KOSPI")
    df_kosdaq = stock.get_market_fundamental_by_ticker(date, market="KOSDAQ")
    df = pd.concat([df_kospi, df_kosdaq])
    df = df.reset_index()  # '티커' 컬럼 유지

    df = df[df['PBR'] > 0]  # 음수/0 제거
    df['bm_ratio'] = 1 / df['PBR']

    # 2. 상위/하위 30%로 분할
    df_sorted = df.sort_values(by='bm_ratio', ascending=False).reset_index(drop=True)
    n = len(df_sorted)
    high = df_sorted.head(int(n * 0.3)).copy()
    low = df_sorted.tail(int(n * 0.3)).copy()

    # 3. 수익률 계산 함수
    def get_return(ticker):
        try:
            df = stock.get_market_ohlcv_by_date(prev_month, date, ticker)
            if df.empty or len(df) < 2:
                return None
            return (df['종가'].iloc[-1] - df['종가'].iloc[0]) / df['종가'].iloc[0]
        except:
            return None

    high['return'] = high['티커'].apply(get_return)
    low['return'] = low['티커'].apply(get_return)

    high_ret = high['return'].dropna().mean()
    low_ret = low['return'].dropna().mean()
    hml = round(high_ret - low_ret, 6)

    return hml

from pykrx import stock, bond

### ✅ 시장 수익률 계산
def get_market_return(start_date: str, end_date: str, index_code: str):
    df = stock.get_index_ohlcv_by_date(start_date, end_date, index_code, freq='m')
    df['수익률'] = df['종가'].pct_change()
    return df

### ✅ 무위험 수익률 (Rf) 계산: CD(91일) 기준
def get_risk_free_rate(date: str):
    try:
        df = bond.get_otc_treasury_yields(date)
        if df.empty:
            return None

        # CD(91일)이 있으면 반환
        if "CD(91일)" in df.index:
            return float(df.loc["CD(91일)", "수익률"]) / 100

        # 없으면 국고채 1년으로 대체
        elif "국고채 1년" in df.index:
            return float(df.loc["국고채 1년", "수익률"]) / 100

        # 그 외는 실패
        else:
            return None
    except Exception as e:
        print(f"[무위험수익률 조회 오류] {date}: {e}")
        return None

### ✅ MKT - RF 계산
def calculate_mkt_rf_all(start_date: str, end_date: str, rf_date: str):
    rf = get_risk_free_rate(rf_date)
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

### ✅ MOM 계산 (3개월 수익률 기준)
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
        except:
            continue

    if not momentum_data:
        raise ValueError("모멘텀 계산을 위한 데이터가 없습니다.")

    # 수익률 기준 정렬
    sorted_data = sorted(momentum_data, key=lambda x: x[1], reverse=True)
    n = len(sorted_data)
    top30 = sorted_data[:int(n * 0.3)]
    bottom30 = sorted_data[-int(n * 0.3):]

    # 평균 수익률 계산
    top_avg = sum([x[1] for x in top30]) / len(top30)
    bottom_avg = sum([x[1] for x in bottom30]) / len(bottom30)

    mom = round(top_avg - bottom_avg, 6)
    return mom


### ✅ 저장 ###
def save_factors_json(date_str, smb_value, hml_value, mom_value, mkt_rf_dict, output_path="./data/stock/output/factor_result.json"):
    result = {
        "date": date_str,
        "smb": smb_value,
        "hml": hml_value,
        "mom": mom_value,
        "mkt_rf": mkt_rf_dict
    }
    os.makedirs(os.path.dirname(output_path), exist_ok=True)
    with open(output_path, "w", encoding="utf-8-sig") as f:
        json.dump(result, f, indent=2, ensure_ascii=False)

### ✅ 실행 ###
if __name__ == "__main__":
    # 기준일 설정
    analysis_date = "20250801"
    result_date = "20250731"
    mkt_start = "20250601"
    output_path = "./data/stock/output/factor_result.json"

    # 계산
    smb = calculate_smb(analysis_date)
    hml = calculate_hml(analysis_date)
    mom = calculate_mom(analysis_date)
    mkt_rf_dict = calculate_mkt_rf_all(mkt_start, analysis_date, result_date)

    # 저장
    save_factors_json(result_date, smb, hml, mom, mkt_rf_dict, output_path)

    print(f"SMB: {smb}, HML: {hml}, MOM: {mom}, MKT-RF: {mkt_rf_dict} → 저장 완료: {output_path}")
