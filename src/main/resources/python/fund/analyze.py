import json
import csv
import os
import numpy as np

input_path = './data/fund/input/input.json'
output_path = './data/fund/output/output.csv'

# 1. JSON 입력 읽기
with open(input_path, "r", encoding="utf-8") as f:
    fund_data = json.load(f)

# 2. 효용 계산 및 결과 정리
results = []
for fund in fund_data:
    id = fund["id"]
    A = float(fund["fundRiskAversion"])
    returns_raw = fund["fundReturnsData"]
    returns_dict = json.loads(returns_raw)

    # 수익률을 리스트로 받아오기 (퍼센트로 들어오므로 /100 필요)
    returns = np.array(list(returns_dict.values()), dtype=np.float64) / 100

    # 누적 복리 수익률 계산
    cumulative_return = np.prod(1 + returns) - 1

    # 연환산 복리 수익률
    annualized_return = (1 + cumulative_return) ** (252 / len(returns)) - 1

    # 연환산 분산 (일간 수익률 기준)
    annualized_variance = np.var(returns, ddof=1) * 252

    # 효용 함수
    utility = annualized_return - 0.5 * A * annualized_variance

    results.append({
        "id": id,
        "expectedReturn": annualized_return,
        "variance": annualized_variance,
        "utility": utility
    })

# 3. CSV로 저장
os.makedirs(os.path.dirname(output_path), exist_ok=True)
with open(output_path, "w", newline="", encoding="utf-8-sig") as f:
    writer = csv.DictWriter(f, fieldnames=["id", "expectedReturn", "variance", "utility"])
    writer.writeheader()
    writer.writerows(results)

print("CSV 저장 완료: output.csv")
