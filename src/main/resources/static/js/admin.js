// Array global que guarda o lote de presentes temporariamente na memória do navegador
let stagedGifts = [];

// 1. FUNÇÃO: Upload imediato da foto assim que o usuário seleciona o arquivo
function uploadPhoto(input) {
    const file = input.files[0];
    if (!file) return;

    const spinner = document.getElementById('uploadSpinner');
    const successText = document.getElementById('uploadSuccess');
    const btnAdd = document.getElementById('btnAddToList');

    spinner.classList.remove('d-none');
    successText.classList.add('d-none');
    btnAdd.disabled = true;

    const formData = new FormData();
    formData.append('file', file);

    // Dispara para a nossa API Rest que salva o arquivo físico e retorna o link virtual
    fetch('/api/gifts/photo', {
        method: 'POST',
        body: formData
    })
    .then(response => {
        if (!response.ok) throw new Error('Falha no upload');
        return response.text();
    })
    .then(photoUrl => {
        document.getElementById('giftPhotoUrl').value = photoUrl;
        spinner.classList.add('d-none');
        successText.classList.remove('d-none');
        btnAdd.disabled = false; // Libera o botão de adicionar à lista
    })
    .catch(error => {
        spinner.classList.add('d-none');
        alert('Erro ao fazer upload da imagem: ' + error.message);
    });
}

// 2. FUNÇÃO: Adiciona os dados do formulário na tabela/lista temporária
function addGiftToList(event) {
    event.preventDefault();

    const name = document.getElementById('giftName').value;
    const price = document.getElementById('giftPrice').value;
    const description = document.getElementById('giftDescription').value;
    const photoUrl = document.getElementById('giftPhotoUrl').value;

    // Cria o objeto idêntico à Entidade Java
    const gift = { name, price: parseFloat(price), description, photoUrl };
    stagedGifts.push(gift);

    // Limpa o formulário e reseta estados de upload
    document.getElementById('giftForm').reset();
    document.getElementById('uploadSuccess').classList.add('d-none');
    document.getElementById('btnAddToList').disabled = true;

    renderTable();
}

// 3. FUNÇÃO: Remove um presente específico da lista temporária
function removeGiftFromList(index) {
    stagedGifts.splice(index, 1);
    renderTable();
}

// 4. FUNÇÃO: Renderiza as linhas da tabela de presentes com base no array stagedGifts
function renderTable() {
    const tbody = document.getElementById('giftsTableBody');
    tbody.innerHTML = '';

    if (stagedGifts.length === 0) {
        tbody.innerHTML = `
            <tr id="emptyRow">
                <td colspan="5" class="text-center text-muted py-4">Nenhum presente adicionado ao lote ainda.</td>
            </tr>`;
        document.getElementById('btnSaveBatch').disabled = true;
        document.getElementById('batchCount').innerText = '0';
        return;
    }

    stagedGifts.forEach((gift, index) => {
        const row = `
            <tr>
                <td><img src="${gift.photoUrl}" class="preview-img" alt="Preview"></td>
                <td><strong>${gift.name}</strong></td>
                <td>R$ ${gift.price.toFixed(2)}</td>
                <td class="text-truncate" style="max-width: 200px;">${gift.description || '-'}</td>
                <td class="text-end">
                    <button class="btn btn-outline-danger btn-sm" onclick="removeGiftFromList(${index})">✕ Retirar</button>
                </td>
            </tr>`;
        tbody.innerHTML += row;
    });

    document.getElementById('btnSaveBatch').disabled = false;
    document.getElementById('batchCount').innerText = stagedGifts.length;
}

// 5. FUNÇÃO: Envia a lista JSON completa para persistência em lote no banco de dados (API)
function saveGiftsBatch() {
    const btnSave = document.getElementById('btnSaveBatch');
    btnSave.disabled = true;
    btnSave.innerText = 'Processando...';

    fetch('/api/gifts/batch', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(stagedGifts)
    })
    .then(response => {
        if (!response.ok) throw new Error('Erro ao salvar o lote');
        return response.json();
    })
    .then(data => {
        alert(`Sucesso! ${data.length} presentes salvos e disponíveis no catálogo oficial.`);
        stagedGifts = []; // Limpa o lote da memória
        renderTable();
    })
    .catch(error => {
        alert(error.message);
        btnSave.disabled = false;
        renderTable();
    });
}

// 6. FUNÇÃO: Upload e processamento em lote da planilha CSV de convidados
function uploadCsvGuests() {
    const fileInput = document.getElementById('csvFile');
    const file = fileInput.files[0];
    const alertBox = document.getElementById('csvAlert');

    if (!file) {
        showAlert('Por favor, selecione um arquivo antes de enviar.', 'alert-danger');
        return;
    }

    const formData = new FormData();
    formData.append('file', file);

    showAlert('Processando planilha e salvando convidados...', 'alert-info');

    fetch('/api/guests/upload-csv', {
        method: 'POST',
        body: formData
    })
    .then(async response => {
        const text = await response.text();
        if (!response.ok) {
            try {
                const errJson = JSON.parse(text);
                throw new Error(errJson.message);
            } catch(e) {
                throw new Error(text || 'Falha ao processar o arquivo.');
            }
        }
        return text;
    })
    .then(message => {
        showAlert('🎉 ' + message, 'alert-success');
        fileInput.value = ''; // Reseta o campo de arquivo
    })
    .catch(error => {
        showAlert('Erro: ' + error.message, 'alert-danger');
    });
}

function showAlert(message, bootstrapClass) {
    const alertBox = document.getElementById('csvAlert');
    alertBox.className = `alert ${bootstrapClass} mt-3`;
    alertBox.innerText = message;
    alertBox.classList.remove('d-none');
}