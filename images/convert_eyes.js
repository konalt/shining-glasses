const { createCanvas, loadImage } = require("canvas");
const fs = require("fs");

const eyeImages = fs.readdirSync("./eyes");

console.log(eyeImages);

async function eyeImageToString(filename) {
    const outPixelsOn = [];

    const image = await loadImage(filename);
    const c = createCanvas(36, 12);
    const ctx = c.getContext("2d");
    ctx.drawImage(image, 36 / 2 - image.width / 2, 0, image.width, 12);

    const data = ctx.getImageData(0, 0, 36, 12);
    for (let i = 0; i < data.data.length; i += 4) {
        const x = (i / 4) % 36;
        const y = Math.floor(i / 4 / 36);
        if (data.data[i] > 128) {
            outPixelsOn.push([x, y]);
        }
    }

    const outText = `${outPixelsOn.map((p) => p.join(",")).join(" ")}`;

    return outText;
}

function indent(n = 1) {
    return " ".repeat(n * 4);
}

async function run() {
    const out = [];
    for (const fn of eyeImages) {
        const id = fn.split(".")[0];
        const string = await eyeImageToString("./eyes/" + fn);
        out.push(`${indent(2)}EYES.put("${id}", "${string}");`);
    }

    const final = out.join("\n");

    if (process.argv[2]) {
        fs.writeFileSync(process.argv[2], final);
    }

    console.log(final);
}

run();
