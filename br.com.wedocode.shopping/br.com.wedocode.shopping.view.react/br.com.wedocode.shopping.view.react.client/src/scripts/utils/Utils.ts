
export function formatValue(value: number | undefined, decimalPoint = 2) {
    if (value == null) {
        return ""
    }

    if (isNaN(value)) {
        console.error("Valor informado não é de tipo numérico!");
        return "";
    }

    return value.toLocaleString(navigator.language, { minimumFractionDigits: decimalPoint });
}

export function formatDate(dateOrMillis: number | Date | null | undefined) {
    if (dateOrMillis === undefined || dateOrMillis == null) {
        console.error("Data não informada, ou inválida!");
        return "Data indisponível";
    }

    const purchaseDate = typeof dateOrMillis === 'number' ? new Date(dateOrMillis) : dateOrMillis

    let purchaseDay = purchaseDate.getDay() + 1; //Dia primeiro retorna zero
    let purchaseMonth = purchaseDate.getMonth() + 1; //Janeiro retorna zero
    let purchaseFullYear = purchaseDate.getFullYear();

    let dayOutput = purchaseDay < 10 ? ("0" + purchaseDay) : purchaseDay;
    let monthOutput = purchaseMonth < 10 ? ("0" + purchaseMonth) : purchaseMonth;

    return dayOutput + "/" + monthOutput + "/" + purchaseFullYear;
}

export function itemImageRequest(itemId: unknown) {
    return `./image/product/${itemId}.png`
}

export function deleteProperties(objectToClean: object) {
    for (const x in objectToClean) {
        if (objectToClean.hasOwnProperty(x)) {
            delete objectToClean[x];
        }
    }
}

export function makeUniqueId() {
    return crypto.randomUUID() as string;
}