import { useEffect, useState } from "react";
import { ActivityIndicator, FlatList, Pressable, ScrollView, StyleSheet, Text, View } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { useTheme } from "../../hooks/useTheme";

import { AdBanner } from "../../components/AdBanner";

import { EmptyState } from "../../components/EmptyState";
import { ErrorState } from "../../components/ErrorState";
import { PlayerCard } from "../../components/PlayerCard";
import { PlayerRankingList } from "../../components/PlayerRankingList";
import { SearchBar } from "../../components/SearchBar";
import { usePlayerRankings } from "../../hooks/usePlayerRankings";
import { usePlayerSearch } from "../../hooks/usePlayerSearch";

const HITTER_CATEGORIES = ["AVG", "HR", "RBI", "H"];
const PITCHER_CATEGORIES = ["ERA", "SO", "W", "SV"];

export default function PlayersScreen() {
  const { colors } = useTheme();

  const [keyword, setKeyword] = useState("");
  const [debounced, setDebounced] = useState("");
  const [type, setType] = useState<"hitter" | "pitcher">("hitter");
  const [hitterCategory, setHitterCategory] = useState(HITTER_CATEGORIES[0]);
  const [pitcherCategory, setPitcherCategory] = useState(PITCHER_CATEGORIES[0]);

  useEffect(() => {
    const handle = setTimeout(() => setDebounced(keyword.trim()), 500);
    return () => clearTimeout(handle);
  }, [keyword]);

  const isSearching = debounced.length >= 2;
  const category = type === "hitter" ? hitterCategory : pitcherCategory;
  const categories = type === "hitter" ? HITTER_CATEGORIES : PITCHER_CATEGORIES;
  const setCategory = type === "hitter" ? setHitterCategory : setPitcherCategory;

  const searchQuery = usePlayerSearch(debounced);
  const rankingsQuery = usePlayerRankings(category, type);

  return (
    <SafeAreaView style={[styles.container, { backgroundColor: colors.background }]} edges={["bottom"]}>
      <View style={styles.searchBar}>
        <SearchBar value={keyword} onChangeText={setKeyword} />
      </View>

      {isSearching ? (
        searchQuery.isLoading ? (
          <View style={styles.center}>
            <ActivityIndicator color={colors.primary} />
          </View>
        ) : searchQuery.isError ? (
          <ErrorState onRetry={() => searchQuery.refetch()} />
        ) : (searchQuery.data ?? []).length === 0 ? (
          <EmptyState message="검색 결과가 없습니다" />
        ) : (
          <FlatList
            data={searchQuery.data ?? []}
            keyExtractor={(item) => item.playerId}
            contentContainerStyle={styles.listContent}
            ItemSeparatorComponent={() => <View style={{ height: 8 }} />}
            renderItem={({ item }) => <PlayerCard player={item} />}
          />
        )
      ) : (
        <View style={{ flex: 1 }}>
          <View style={[styles.toggleRow, { borderColor: colors.border }]}>
            <Pressable
              onPress={() => setType("hitter")}
              style={[styles.toggleButton, type === "hitter" ? { backgroundColor: colors.primary } : null]}
            >
              <Text style={[styles.toggleText, { color: type === "hitter" ? "#FFFFFF" : colors.text }]}>
                타자
              </Text>
            </Pressable>
            <Pressable
              onPress={() => setType("pitcher")}
              style={[styles.toggleButton, type === "pitcher" ? { backgroundColor: colors.primary } : null]}
            >
              <Text style={[styles.toggleText, { color: type === "pitcher" ? "#FFFFFF" : colors.text }]}>
                투수
              </Text>
            </Pressable>
          </View>

          <ScrollView
            horizontal
            showsHorizontalScrollIndicator={false}
            style={styles.categoryScroll}
            contentContainerStyle={styles.categoryRow}
          >
            {categories.map((cat) => (
              <Pressable
                key={cat}
                onPress={() => setCategory(cat)}
                style={({ pressed }) => [
                  styles.categoryChip,
                  {
                    backgroundColor: category === cat ? colors.primary : colors.card,
                    borderColor: category === cat ? colors.primary : colors.border,
                    opacity: pressed ? 0.6 : 1,
                  },
                ]}
              >
                <Text
                  style={[styles.categoryText, { color: category === cat ? "#FFFFFF" : colors.text }]}
                >
                  {cat}
                </Text>
              </Pressable>
            ))}
          </ScrollView>

          <ScrollView contentContainerStyle={styles.listWrap}>
            {rankingsQuery.isLoading ? (
              <View style={styles.center}>
                <ActivityIndicator color={colors.primary} />
              </View>
            ) : rankingsQuery.isError ? (
              <ErrorState onRetry={() => rankingsQuery.refetch()} />
            ) : (
              <PlayerRankingList rankings={rankingsQuery.data ?? []} />
            )}
            <AdBanner />
          </ScrollView>
        </View>
      )}
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  searchBar: { padding: 12 },
  listContent: { padding: 12, gap: 10 },
  toggleRow: {
    flexDirection: "row",
    marginHorizontal: 12,
    marginBottom: 8,
    borderWidth: 1,
    borderRadius: 8,
    overflow: "hidden",
  },
  toggleButton: { flex: 1, paddingVertical: 10, alignItems: "center" },
  toggleText: { fontSize: 14, fontWeight: "600" },
  // horizontal ScrollView 가 column 부모 안에서 세로로 stretch 되지 않도록 flexGrow: 0
  categoryScroll: { flexGrow: 0 },
  categoryRow: { paddingHorizontal: 12, paddingVertical: 4, gap: 8, alignItems: "center" },
  categoryChip: { paddingHorizontal: 14, paddingVertical: 8, borderRadius: 18, borderWidth: 1 },
  categoryText: { fontSize: 13, fontWeight: "600" },
  listWrap: { padding: 12 },
  center: { flex: 1, alignItems: "center", justifyContent: "center" },
});
