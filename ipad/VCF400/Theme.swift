import SwiftUI

/// モダン iPadOS UI (グリーンスクリーンの模写はしない)。
enum Theme {
    static let brand = Color(red: 0.12, green: 0.25, blue: 0.69)
    static let bg = Color(red: 0.96, green: 0.965, blue: 0.98)
    static let card = Color.white
    static let danger = Color(red: 0.73, green: 0.11, blue: 0.11)
    static let dangerBg = Color(red: 1.0, green: 0.95, blue: 0.95)
    static let ok = Color(red: 0.02, green: 0.47, blue: 0.34)
}

struct Card<Content: View>: View {
    @ViewBuilder var content: Content
    var body: some View {
        VStack(alignment: .leading, spacing: 12) { content }
            .padding(20)
            .frame(maxWidth: .infinity, alignment: .leading)
            .background(Theme.card, in: RoundedRectangle(cornerRadius: 14))
            .shadow(color: .black.opacity(0.05), radius: 8, y: 2)
    }
}

/// 画面タイトル (元 DDS の見出し文言を保持)
struct ScreenHeader: View {
    let title: String
    var subtitle: String? = nil
    var path: String
    var body: some View {
        VStack(alignment: .leading, spacing: 2) {
            if let s = subtitle { Text(s).font(.caption).tracking(2).foregroundStyle(.secondary).textCase(.uppercase) }
            Text(title).font(.system(size: 30, weight: .heavy)).foregroundStyle(Theme.brand)
                .accessibilityIdentifier("screenTitle")
            Text(path).font(.system(.caption, design: .monospaced)).foregroundStyle(.secondary)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
    }
}

/// 入力項目 (番号ラベル付き、元 DDS の順序で並べる)
struct FieldRow<Content: View>: View {
    let step: Int
    let label: String
    var hint: String? = nil
    var error = false
    @ViewBuilder var content: Content
    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            HStack(alignment: .firstTextBaseline, spacing: 10) {
                Text("\(step)").font(.caption.bold()).foregroundStyle(Theme.brand)
                    .frame(width: 26, height: 26).background(Theme.brand.opacity(0.12), in: Circle())
                Text(label).font(.headline)
            }
            content
                .textFieldStyle(.plain)
                .padding(12)
                .background(error ? Theme.dangerBg : Color(.secondarySystemBackground), in: RoundedRectangle(cornerRadius: 10))
                .overlay(RoundedRectangle(cornerRadius: 10).stroke(error ? Theme.danger : Color.gray.opacity(0.3), lineWidth: 1.5))
            if let h = hint { Text(h).font(.caption).foregroundStyle(.secondary) }
        }
        .padding(.vertical, 6)
    }
}

/// ERRLINE (元画面のエラー行)
struct ErrorLine: View {
    let text: String?
    var body: some View {
        if let t = text, !t.isEmpty {
            HStack(spacing: 10) {
                Image(systemName: "exclamationmark.circle.fill")
                Text(t).fontWeight(.semibold)
            }
            .foregroundStyle(Theme.danger)
            .padding(12)
            .frame(maxWidth: .infinity, alignment: .leading)
            .background(Theme.dangerBg, in: RoundedRectangle(cornerRadius: 10))
            .accessibilityIdentifier("errline")
        }
    }
}

/// ファンクションキーボタン: ラベルに元のキー番号を併記
struct FKeyButton: View {
    let title: String
    let key: String            // "F5", "F12", "F3", "F8", "ENTER"
    var primary = false
    var destructive = false
    let action: () -> Void
    var body: some View {
        Button(action: action) {
            HStack(spacing: 8) {
                Text(title).fontWeight(.bold)
                Text(key).font(.system(.caption, design: .monospaced).bold())
                    .padding(.horizontal, 7).padding(.vertical, 2)
                    .background(.white.opacity(primary ? 0.2 : 0.6), in: RoundedRectangle(cornerRadius: 6))
                    .overlay(RoundedRectangle(cornerRadius: 6).stroke(.primary.opacity(0.25)))
            }
            .frame(minWidth: 150).padding(.vertical, 12).padding(.horizontal, 18)
        }
        .buttonStyle(.borderedProminent)
        .tint(primary ? Theme.brand : (destructive ? Theme.danger : Color(.systemGray5)))
        .foregroundStyle(primary || destructive ? .white : .primary)
        .accessibilityIdentifier(key)
    }
}

/// 番号付きメニュー項目 (番号入力の概念を残しつつタップでも選択可)
struct MenuItem: View {
    let number: String
    let title: String
    var enabled = true
    let action: () -> Void
    var body: some View {
        Button(action: action) {
            HStack(spacing: 14) {
                Text(number).font(.system(.body, design: .monospaced).bold()).foregroundStyle(.white)
                    .frame(minWidth: 42, minHeight: 34)
                    .background(enabled ? Theme.brand : Color(.systemGray2), in: RoundedRectangle(cornerRadius: 8))
                Text(title).foregroundStyle(enabled ? .primary : .secondary)
                Spacer()
            }
            .padding(12)
            .background(Color.white, in: RoundedRectangle(cornerRadius: 12))
            .overlay(RoundedRectangle(cornerRadius: 12).stroke(Color.gray.opacity(0.25)))
        }
        .buttonStyle(.plain)
        .disabled(!enabled)
        .accessibilityIdentifier("menu.\(number)")
    }
}

/// 「番号を入力して ENTER」行
struct OptionLine: View {
    let label: String
    @Binding var option: String
    let maxLength: Int
    let action: () -> Void
    var body: some View {
        HStack(spacing: 12) {
            Text(label).font(.headline)
            TextField("", text: $option)
                .keyboardType(.numberPad)
                .font(.system(.title3, design: .monospaced))
                .multilineTextAlignment(.center)
                .frame(width: 110).padding(10)
                .background(Color(.secondarySystemBackground), in: RoundedRectangle(cornerRadius: 10))
                .overlay(RoundedRectangle(cornerRadius: 10).stroke(Color.gray.opacity(0.3)))
                .onChange(of: option) { _, v in if v.count > maxLength { option = String(v.prefix(maxLength)) } }
                .onSubmit(action)
                .accessibilityIdentifier("option")
            FKeyButton(title: "選択", key: "ENTER", primary: true, action: action)
        }
    }
}
