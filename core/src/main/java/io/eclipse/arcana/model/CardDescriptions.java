package io.eclipse.arcana.model;

import java.util.HashMap;
import java.util.Map;

public final class CardDescriptions {
    private static final String SAME = "정방향과 동일";

    public static final class Entry {
        public final String upright;
        public final String reversed;
        public final String keywords;

        private Entry(String upright, String reversed, String keywords) {
            this.upright = upright;
            this.reversed = reversed;
            this.keywords = keywords;
        }
    }

    private static final Map<String, Entry> ENTRIES = new HashMap<>();

    static {
        putMajor();
        putWands();
        putCups();
        putSwords();
        putPentacles();
    }

    private CardDescriptions() {
    }

    public static Entry get(String cardId) {
        return ENTRIES.get(cardId);
    }

    public static String currentText(String cardId, boolean reversed, boolean effectsSwapped) {
        Entry entry = get(cardId);
        if (entry == null) return "";
        boolean useReversed = effectsSwapped ? !reversed : reversed;
        return useReversed ? entry.reversed : entry.upright;
    }

    private static void put(String id, String upright, String reversed, String keywords) {
        ENTRIES.put(id, new Entry(upright, reversed, keywords));
    }

    private static void putMajor() {
        put("Fool",
            "손패 카드 1장을 복제해 추가합니다. 복제 카드는 0코스트이며 효과가 약해집니다.",
            "다음에 드로우한 카드를 확인하지 못하고 즉시 강제로 사용합니다.",
            "복제 / 강제 사용");
        put("Magician",
            "손패 카드 1장을 계약 카드로 만듭니다. 이번 전투에서 0코스트가 되고 효과가 2배가 됩니다.",
            "손패 카드 1장의 환영을 만듭니다. 환영은 0코스트지만 50% 확률로 효과가 사라집니다.",
            "계약 / 환영 / 소멸");
        put("Priestess",
            "덱 위 3장 중 1장을 골라 손패로 가져옵니다.",
            "내 손패의 무작위 카드 2장을 공개합니다.",
            "예지 / 공개");
        put("Empress",
            "즉시 HP를 크게 회복합니다.",
            "이번 턴 카드를 사용할 때마다 그 카드의 코스트가 1~2 증가합니다.",
            "회복 / 코스트 증가");
        put("Emperor",
            "상대의 다음 턴 카드 사용 수를 1장으로 제한합니다.",
            "이번 턴 내 모든 카드가 0코스트가 되지만 대상이 무작위가 됩니다.",
            "사용 제한 / 0코스트 / 랜덤 대상");
        put("Hierophant",
            "상대는 다음 행동 턴 동안 메이저 카드를 사용할 수 없습니다.",
            "이번 턴 내 카드의 정방향 효과와 역방향 효과가 뒤바뀝니다.",
            "메이저 봉쇄 / 방향 전환");
        put("Lovers",
            "나와 상대 손패에서 각각 카드 1장의 코스트를 0으로 만듭니다.",
            "내 손패 중 무작위 카드 1장을 강제로 사용합니다.",
            "코스트 0 / 강제 사용");
        put("Chariot",
            "이번 턴 사용한 카드는 필드로 가지 않고 손패에 유지됩니다.",
            "손패를 모두 버리고 같은 수만큼 다시 드로우합니다.",
            "손패 유지 / 교체");
        put("Strength",
            "이번 턴 내 피해가 증가합니다. HP가 35% 이하이면 추가 피해가 더 붙습니다.",
            "이번 턴 내 카드 효과가 50% 확률로 실패합니다.",
            "피해 증가 / 효과 실패");
        put("Hermit",
            "이번 턴 상대가 나를 대상으로 지정할 수 없습니다.",
            "이번 턴 드로우하는 카드는 모두 역방향이 됩니다.",
            "대상 회피 / 역방향 드로우");
        put("Fortune",
            "상대와 손패를 통째로 교환합니다.",
            "이번 턴 내가 사용하는 카드 효과가 나에게도 함께 적용됩니다.",
            "손패 교환 / 효과 반사");
        put("Justice",
            "상대 손패 수를 내 손패 수에 맞춥니다.",
            "이번 턴 상대가 사용하는 카드 효과를 무효화하고, 내가 그 코스트를 회복합니다.",
            "손패 조정 / 효과 무효");
        put("HangedMan",
            "내 HP 10%를 잃고 손패를 새로 정리합니다.",
            "이번 턴 사용할 수 없고 손패에 묶일 카드 1장을 선택합니다.",
            "희생 / 손패 교체 / 잠금");
        put("Death",
            "필드와 무덤을 리셋하고 양쪽 손패를 5장으로 다시 뽑습니다.",
            "양쪽 모두 2턴 동안 현재 손패로 버팁니다. 손패 교체와 드로우가 제한됩니다.",
            "리셋 / 손패 고정 / 소멸");
        put("Temperance",
            "내 손패의 정방향/역방향 비율을 균형 있게 조정합니다.",
            "내 손패의 정방향/역방향 상태를 모두 반전합니다.",
            "방향 조정 / 반전");
        put("Devil",
            "이번 턴 코스트가 부족해도 HP를 대신 써서 카드를 사용할 수 있습니다.",
            "내게 걸린 제한과 디버프를 즉시 해제합니다.",
            "초과 사용 / 디버프 해제");
        put("Tower",
            "상대는 다음 드로우를 할 수 없습니다.",
            "내 현재 코스트가 1~6 사이 무작위 값으로 바뀝니다.",
            "드로우 봉쇄 / 코스트 변경 / 소멸");
        put("Star",
            "이번 턴 코스트를 전부 쓰면 1장 드로우하고, 다음 턴 시작 코스트가 2배가 됩니다.",
            "2턴 동안 가짜 보호막을 얻습니다. 만료 시 막은 피해보다 큰 피해를 받습니다.",
            "조건부 드로우 / 보호막");
        put("Moon",
            "양쪽 손패를 숨기고 무작위 카드 1장을 강제로 사용합니다.",
            "상대 손패 중 무작위 카드 2장을 공개합니다.",
            "은폐 / 강제 사용 / 공개");
        put("Sun",
            "이번 턴 코스트를 3 회복합니다.",
            "턴 종료 시 남은 코스트만큼 HP를 잃습니다.",
            "코스트 회복 / 잔여 코스트 피해");
        put("Judgement",
            "이번 게임에서 소멸된 카드 최대 2장을 필드에 되살립니다.",
            "이번 턴 이후 사용하는 카드 효과가 1턴 지연됩니다.",
            "부활 / 효과 지연");
        put("World",
            "상대 손패를 공개하고 그중 카드 1장을 내 손패로 가져옵니다.",
            "상대의 다음 턴 시작 코스트를 3으로 고정합니다.",
            "강탈 / 코스트 고정 / 소멸");
    }

    private static void putWands() {
        put("Wands/Ace", "상대에게 피해 5를 줍니다.", SAME, "피해");
        put("Wands/Two", "상대에게 피해 4를 주고, 다음 턴 시작 코스트가 1 증가합니다.", SAME, "피해 / 다음 코스트");
        put("Wands/Three", "상대에게 피해 4를 주고 카드 1장을 드로우합니다.", SAME, "피해 / 드로우");
        put("Wands/Four", "상대에게 피해 3을 주고 HP 5를 회복합니다.", SAME, "피해 / 회복");
        put("Wands/Five", "상대에게 피해 8을 주고, 상대의 다음 드로우를 1장 줄입니다.", SAME, "피해 / 드로우 감소");
        put("Wands/Six", "상대에게 피해 8을 주고, 이번 턴 코스트를 1 회복합니다.", SAME, "피해 / 코스트 회복");
        put("Wands/Seven", "상대에게 피해 8을 주고, 다음에 받는 피해 일부를 반사합니다.", SAME, "피해 / 반사");
        put("Wands/Eight", "상대에게 피해 6을 두 번 줍니다.", SAME, "연속 피해");
        put("Wands/Nine", "상대에게 피해 12를 줍니다. 내 HP가 낮을수록 피해가 증가합니다.", SAME, "피해 / 저HP 보너스");
        put("Wands/Ten", "상대에게 피해 12를 줍니다. 이번 턴 사용한 Wands 카드 수만큼 추가 피해가 붙습니다.", SAME, "피해 / Wands 연계");
        put("Wands/Page", "이번 턴 내 손패의 Wands 카드 코스트를 모두 1 낮춥니다.", "이번 턴 내 손패의 Wands 카드 코스트를 모두 1 높입니다.", "Wands 비용");
        put("Wands/Knight", "상대에게 피해 14를 주고, 내 HP 10을 잃습니다.", "상대에게 피해 20을 주고, 내 HP 25를 잃습니다.", "고위험 피해");
        put("Wands/Queen", "상대에게 피해 13을 주고, 손패의 Wands 카드 코스트를 1 낮춥니다.", "상대와 나 모두에게 피해 17을 줍니다.", "피해 / Wands 비용");
        put("Wands/King", "이번 턴 필드에 낸 다른 Wands 카드 효과를 모두 다시 발동합니다.", "이번 턴 필드에 낸 마지막 Wands 카드 효과만 다시 발동합니다.", "Wands 재발동");
    }

    private static void putCups() {
        put("Cups/Ace", "HP 7을 회복합니다.", SAME, "회복");
        put("Cups/Two", "HP 6을 회복하고 카드 1장을 드로우합니다.", SAME, "회복 / 드로우");
        put("Cups/Three", "손패 카드 1장의 코스트를 1 낮춥니다.", SAME, "코스트 감소");
        put("Cups/Four", "상대에게 피해 3을 주고, 다음 턴 드로우가 1장 증가합니다.", SAME, "피해 / 다음 드로우");
        put("Cups/Five", "HP 11을 회복하고 상대에게 피해 5를 줍니다.", SAME, "회복 / 피해");
        put("Cups/Six", "손패 카드 2장의 코스트를 1 낮춥니다.", SAME, "코스트 감소");
        put("Cups/Seven", "카드 3장을 드로우합니다.", SAME, "드로우");
        put("Cups/Eight", "HP 13을 회복하고, 상대의 다음 드로우를 1장 줄입니다.", SAME, "회복 / 드로우 감소");
        put("Cups/Nine", "HP 15를 회복하고, 이번 턴 코스트를 1 회복합니다.", SAME, "회복 / 코스트 회복");
        put("Cups/Ten", "이번 턴 사용한 Cups 카드 수마다 HP 4를 회복합니다.", SAME, "Cups 연계 / 회복");
        put("Cups/Page", "이번 턴 내 손패의 Cups 카드 코스트를 모두 1 낮춥니다.", "이번 턴 내 손패의 Cups 카드 코스트를 모두 1 높입니다.", "Cups 비용");
        put("Cups/Knight", "이번 턴 내 회복 효과가 2배가 됩니다.", "이번 턴 회복 효과가 2배가 되지만 손패 2장을 버립니다.", "회복 증폭 / 손패 손실");
        put("Cups/Queen", "상대에게 피해 8을 주고 HP 7을 회복합니다.", "상대에게 피해 11을 주고, 상대의 다음 회복 효과를 막습니다.", "피해 / 회복 차단");
        put("Cups/King", "이번 턴 필드에 낸 Cups 카드의 회복 효과를 모두 다시 적용합니다.", "이번 턴 필드에 낸 Cups 카드 효과를 다시 발동하고, 내 코스트를 전부 소모합니다.", "Cups 재발동");
    }

    private static void putSwords() {
        put("Swords/Ace", "상대에게 피해 4를 줍니다.", SAME, "피해");
        put("Swords/Two", "상대에게 피해 3을 주고, 내 현재 코스트를 1 낮춥니다.", SAME, "피해 / 코스트 감소");
        put("Swords/Three", "상대에게 피해 5를 줍니다.", SAME, "피해");
        put("Swords/Four", "이번 턴 내가 받는 피해를 절반으로 줄입니다.", SAME, "피해 감소");
        put("Swords/Five", "상대에게 피해 6을 주고, 상대 손패 1장을 버립니다.", SAME, "피해 / 버리기");
        put("Swords/Six", "상대에게 피해 6을 주고, 상대의 다음 드로우를 1장 줄입니다.", SAME, "피해 / 드로우 감소");
        put("Swords/Seven", "상대에게 피해 7을 주고, 상대 손패 1장을 훔쳐 공개 상태로 가져옵니다.", SAME, "피해 / 훔치기");
        put("Swords/Eight", "상대에게 피해 7을 주고 상대 코스트를 2 낮춥니다. 내 코스트는 1 회복합니다.", SAME, "피해 / 코스트 조작");
        put("Swords/Nine", "상대에게 피해 10을 주고, 상대 손패 1장을 공개합니다.", SAME, "피해 / 공개");
        put("Swords/Ten", "상대에게 피해 11을 주고, 상대의 다음 턴 카드 사용 수를 1장으로 제한합니다.", SAME, "피해 / 사용 제한");
        put("Swords/Page", "이번 턴 내 손패의 Swords 카드 코스트를 모두 1 낮춥니다.", "이번 턴 내 손패의 Swords 카드 코스트를 모두 1 높입니다.", "Swords 비용");
        put("Swords/Knight", "상대 손패 2장을 공개하고, 상대 손패 1장을 버립니다.", "상대에게 피해 13을 주고, 내 손패 1장을 버립니다.", "공개 / 버리기");
        put("Swords/Queen", "상대에게 피해 10을 주고, 상대 손패 3장을 공개합니다.", "상대에게 피해 11을 주고, 내 손패 2장을 상대에게 공개합니다.", "피해 / 공개");
        put("Swords/King", "이번 턴 필드에 낸 다른 Swords 카드 효과를 모두 다시 발동합니다.", "이번 턴 필드에 낸 마지막 Swords 카드 효과를 나에게 발동합니다.", "Swords 재발동");
    }

    private static void putPentacles() {
        put("Pentacles/Ace", "이번 턴 코스트를 2 회복합니다.", SAME, "코스트 회복");
        put("Pentacles/Two", "상대에게 피해 3을 주고, 이번 턴 코스트를 1 회복합니다.", SAME, "피해 / 코스트 회복");
        put("Pentacles/Three", "손패의 숫자 마이너 카드 1장을 0코스트로 만듭니다.", SAME, "0코스트");
        put("Pentacles/Four", "이번 턴 남은 코스트의 절반을 다음 턴으로 넘깁니다.", SAME, "코스트 이월");
        put("Pentacles/Five", "상대에게 피해 6을 주고, 이번 턴 코스트를 2 회복합니다.", SAME, "피해 / 코스트 회복");
        put("Pentacles/Six", "상대 코스트를 2 낮추고, 내 코스트를 2 회복합니다.", SAME, "코스트 조작");
        put("Pentacles/Seven", "이번 턴 필드에 낸 카드들의 기본 코스트 합만큼 피해를 줍니다. 최대 15 피해.", SAME, "코스트 비례 피해");
        put("Pentacles/Eight", "상대에게 피해 7을 주고, 다음 턴 시작 코스트가 4 증가합니다.", SAME, "피해 / 다음 코스트");
        put("Pentacles/Nine", "손패의 Pentacles 카드 코스트를 모두 1 낮추고, 상대에게 피해 9를 줍니다.", SAME, "피해 / Pentacles 비용");
        put("Pentacles/Ten", "이번 턴 사용한 Pentacles 카드 수마다 피해 3을 줍니다.", SAME, "Pentacles 연계 / 피해");
        put("Pentacles/Page", "이번 턴 내 손패의 Pentacles 카드 코스트를 모두 1 낮춥니다.", "이번 턴 내 손패의 Pentacles 카드 코스트를 모두 1 높입니다.", "Pentacles 비용");
        put("Pentacles/Knight", "현재 코스트를 모두 소모합니다. 소모한 코스트마다 50% 확률로 피해 6을 줍니다.", "현재 코스트를 모두 소모해 그 1.5배 피해를 주고, 소모한 코스트만큼 HP를 잃습니다.", "코스트 소모 / 도박 피해");
        put("Pentacles/Queen", "상대에게 피해 9를 주고, 상대의 다음 턴 시작 코스트를 3 낮춥니다.", "상대에게 피해 15를 주지만 내 손패를 모두 버리고 다음 드로우가 막힙니다.", "피해 / 코스트 압박");
        put("Pentacles/King", "이번 턴 필드에 낸 다른 Pentacles 카드의 기본 코스트 합 x3 피해를 줍니다.", "이번 턴 필드에 낸 다른 Pentacles 카드의 기본 코스트 합 x2 피해를 나에게 줍니다.", "Pentacles 연계 피해");
    }
}
