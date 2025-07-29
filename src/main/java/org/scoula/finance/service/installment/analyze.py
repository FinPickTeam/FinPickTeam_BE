import json
import csv
import sys
import re
import os

# 사용자 조건 키워드와 대응되는 텍스트 키워드
condition_keywords = {
    "autoTransfer": ["자동이체", "이체실적", "계좌간 이체", "납입", "납입실적", "계약월수의 1/2", "자동이체 출금"],
    "cardUsage": ["카드", "신용카드", "체크카드", "결제실적", "카드사용", "카드이용"],
    "openBanking": ["오픈뱅킹", "타행계좌", "오픈뱅킹서비스"],
    "salaryTransfer": ["급여이체", "연금이체"],
    "utilityPayment": ["공과금", "공과금 자동이체", "자동납부", "지로"],
    "marketingConsent": ["마케팅", "정보수신", "수신동의", "마케팅동의", "개인정보", "개인(신용)정보", "전화 및 SMS 동의"],
    "housingSubscription": ["청약", "주택청약", "청약통장", "주택청약종합저축"],
    "internetMobileBanking": ["인터넷뱅킹", "모바일뱅킹", "비대면", "폰뱅킹", "모바일채널", "인터넷/모바일"],
    "greenMission": ["탄소", "대중교통", "온실가스", "친환경", "환경", "종이통장", "플라스틱", "감축", "미션", "에너지절감"],
    "incomeTransfer": ["소득이체", "소득", "소득이전"],
    "newCustomer": ["신규고객", "처음", "첫거래", "최초", "첫 거래"]
}

def load_input(input_path):
    with open(input_path, 'r', encoding='utf-8') as f:
        return json.load(f)

def check_conditions(user_conditions, product_text):
    matched = 0
    for key, keywords in condition_keywords.items():
        if user_conditions.get(key):
            if any(kw in product_text for kw in keywords):
                matched += 1
    return matched

def analyze():
    input_path = './data/installment/input/input.json'
    output_path = './data/installment/output/output.csv'

    print("현재 작업 디렉토리:", os.getcwd())
    print("입력 파일 경로:", input_path)
    print("출력 파일 경로:", output_path)

    data = load_input(input_path)
    user_conditions = data['userCondition']
    products = data['products']

    results = []

    for product in products:
        name = product['name']
        text = product['conditionText']
        matched_count = check_conditions(user_conditions, text)

        if matched_count == 0:
            total_rate = 0.0
        else:
            rate_matches = re.findall(r'([0-9.]+)\s*%p', text)
            rate_values = [float(r) for r in rate_matches]
            total_rate = sum(rate_values) if rate_values else 0.0

        results.append({
            'name': name,
            'matchedCount': matched_count,
            'totalRate': round(total_rate, 3)
        })

    os.makedirs(os.path.dirname(output_path), exist_ok=True)
    with open(output_path, 'w', newline='', encoding='utf-8-sig') as f:
        writer = csv.DictWriter(f, fieldnames=['name', 'matchedCount', 'totalRate'])
        writer.writeheader()
        writer.writerows(results)

    print("분석 완료! CSV 저장됨.")

if __name__ == "__main__":
    analyze()
